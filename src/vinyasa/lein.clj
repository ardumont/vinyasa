(ns vinyasa.lein
  (:require [leiningen.core.main :as lein]
            [leiningen.core.project :as project]))

(def ^:dynamic *project*
  (try (project/read "project.clj")
       (catch Exception e)))
       
(if (nil? *project*)
  (println "WARNING: vinyasa.lein/lein will not work without project.clj"))

(defmacro lein [& args]
  `(binding [lein/*exit-process?* false]
     (lein/-main ~@(map str args))))
