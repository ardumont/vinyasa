(ns vinyasa.core
  (:require [cemerick.pomegranate]
            [clojure.tools.namespace.repl]
            [clojure.stacktrace]
            [clj-stacktrace.repl]
            [clojure.pprint]
            [clojure.repl]
            [spyscope.core]))

(alter-var-root #'clojure.stacktrace/print-cause-trace
                (constantly clj-stacktrace.repl/pst))
(intern 'clojure.core 'refresh clojure.tools.namespace.repl/refresh)
(intern 'clojure.core 'pprint  clojure.pprint/pprint)
(intern 'clojure.core
        (with-meta 'doc {:macro true})
        @#'clojure.repl/doc)
(intern 'clojure.core
        (with-meta 'source {:macro true})
        @#'clojure.repl/source)

(defn pull-fn [lib]
  (cemerick.pomegranate/add-dependencies
   :coordinates [[(symbol lib) "RELEASE"]]
   :repositories {"clojars" "http://clojars.org/repo"
                  "central" "http://repo1.maven.org/maven2/"}))

(intern 'clojure.core 'pull pull-fn)


(defn all-methods [x]
    (->> x reflect 
           :members 
           (filter :return-type)  
           (map :name) 
           sort 
           (map #(str "." %))
           distinct
           println))
           
           