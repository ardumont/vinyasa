(ns vinyasa.reimport
  (:require [leiningen.core.project :as project]
            [leiningen.core.eval :as eval]
            [leiningen.javac :as javac]
            [clojure.walk :refer [postwalk]]
            [clojure.repl :refer [source-fn]]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]))

(defn- run-javac
  [project args]
  (let [compile-path (:compile-path project)
        files (#'javac/stale-java-sources (:java-source-paths project) compile-path)
        javac-opts (vec (#'javac/javac-options project files args))
        form (#'javac/subprocess-form compile-path files javac-opts)]
    (when (seq files)
      (binding [eval/*pump-in* false]
        (eval/eval-in
         (project/merge-profiles project [javac/subprocess-profile])
         form)))))

(defn to-byte-array [^java.io.File x]
  (with-open [buffer (java.io.ByteArrayOutputStream.)]
    (io/copy x buffer)
    (.toByteArray buffer)))

(defn reimport-from-file
  [classname f]
  (.defineClass (clojure.lang.DynamicClassLoader.)
                classname
                (to-byte-array f)
                nil)
  (println (format "'%s' imported from %s"  classname (.getPath f)))
  (.importClass @#'clojure.core/*ns* (Class/forName classname)))

(defn reimport-path-from-dir
  [dir path]
  (let [classname (->> (clojure.string/split
                        (-> (re-find #"(.*).class" path)
                            second)
                        (re-pattern java.io.File/separator))
                       (clojure.string/join "."))
        f (io/file (str dir "/" path))]
    (reimport-from-file classname f)))

(defn reimport-class-from-dir
  [dir classname]
  (let [file-path (->> (clojure.string/split classname #"\." )
                       (clojure.string/join java.io.File/separator))
        f (io/file (str dir "/" file-path ".class"))]
    (reimport-from-file classname f)))

(defn reimport-reload
  [dir]
  (let [paths (->> (file-seq (io/file dir))
                 (map #(.getPath %))
                 (filter #(re-find #".class$" %))
                 (map #(subs % (inc (count dir)))))]
    (doseq [path paths]
      (reimport-path-from-dir dir path))))

(defn make-reload-path [project]
  (str (:target-path project)
       java.io.File/separator
       (or (:target-reload-path project)
           "reload")))

(defn reimport-compile [project args]
  (let [reload-path (make-reload-path project)
        compile-path (:compile-path project)]
    (try (sh/sh "rm" "-f" compile-path)
         (if (.exists (io/file reload-path))
           (sh/sh "mv" reload-path compile-path))
         (run-javac project args)
         reload-path
         (catch Exception e
           (println "Failed: Java files did not properly compile:"
                    (pr-str (vec (:java-source-paths project )))))
         (finally
           (sh/sh "mv" compile-path reload-path)))))

(defn reimport-selected-list [args]
  (mapcat (fn [x]
            (cond (or (seq? x) (vector? x))
                  (let [pkg (str (first x))]
                    (map #(str pkg "." (str %)) (rest x)))
                  :else [(str x)]))
          args))

(defn reimport-
  ([] (reimport- nil))
  ([classes] (reimport- vinyasa.lein/*project* classes))
  ([project classes]
      (let [reload-path (make-reload-path project)]
        (cond (or (empty? classes)
                  (= :all (first classes)))
              (reimport-reload reload-path)

              :else
              (doseq [cl (reimport-selected-list classes)]
                (reimport-class-from-dir reload-path cl))))))

(defn reimport
  ([] (reimport nil))
  ([classes]
      (let [project vinyasa.lein/*project*]
        (if-let [reload-path (reimport-compile project nil)]
          (reimport- project classes)))))
