(ns vinyasa.reimport
  (:require [leiningen.core.project :as project]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [vinyasa.lein]))

(def ^:dynamic *target-dir*
      (:target-path vinyasa.lein/*project*))

(defn to-byte-array [^java.io.File x]
  (with-open [buffer (java.io.ByteArrayOutputStream.)]
    (io/copy x buffer)
    (.toByteArray buffer)))

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
  (println (format "'%s' imported from %s"  classname (.getPath f))))

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

(defn reimport-compile [target-path]
  (let [reload-path (str target-path java.io.File/separator
                         (or (:target-reload-path vinyasa.lein/*project*)
                             "reload"))]
    (vinyasa.lein/lein javac)
    (sh/sh "rm" "-R" reload-path)
    (sh/sh "mv" (str target-path java.io.File/separator "classes")
           reload-path)
    reload-path))

(defn reimport-all
  [target-path]
  (if target-path
    (let [reload-path (reimport-compile target-path)]
      (reimport-reload reload-path))
    (throw (Exception. "No target directory found"))))

(defn reimport-selected-list [args]
  (mapcat (fn [x]
            (cond (or (seq? x) (vector? x))
                  (let [pkg (str (first x))]
                    (map #(str pkg "." (str %)) (rest x)))
                  :else [(str x)]))
          args))

(defn reimport-selected [target-path args]
  (if target-path
    (let [reload-path (reimport-compile target-path)]
      (doseq [cl (reimport-selected-list args)]
        (reimport-class-from-dir reload-path cl)))
    (throw (Exception. "No target directory found"))))

(defn reimport
  ([] (reimport :all))
  ([classes]
     (cond (or (= :all classes)
               (empty? classes))
           (reimport-all *target-dir*)

           :else
           (reimport-selected *target-dir* classes))))
