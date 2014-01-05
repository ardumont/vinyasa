(ns vinyasa.lein
  (:require [clojure.java.io :as io]
            [clojure.walk :refer [postwalk]]
            [clojure.repl :refer [source-fn]]
            [cemerick.pomegranate :as pom]
            [leiningen.core.main :as lein]
            [leiningen.core.project :as project]))

(def lein-main-form
  (postwalk
   (fn [f]
     (cond (and (list? f) (= 'exit (first f))) nil
           (list? f) (filter (comp not nil?) f)
           :else f))
   (read-string (source-fn 'leiningen.core.main/-main))))

(in-ns 'leiningen.core.main)
(eval vinyasa.lein/lein-main-form)
(in-ns 'vinyasa.lein)

(def ^:dynamic *project*
  (try (project/read "project.clj")
       (catch Exception e)))
       
(if (nil? *project*)
  (println "WARNING: vinyasa.lein/lein will not work without project.clj"))

(defmacro lein [& args]
  `(leiningen.core.main/-main ~@(map str args)))
