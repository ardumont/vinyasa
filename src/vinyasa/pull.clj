(ns vinyasa.pull
  (:require [cemerick.pomegranate]))

(defn pull
  ([lib] (pull lib "RELEASE"))
  ([lib release]
     (cemerick.pomegranate/add-dependencies
           :coordinates [[lib release]]
           :repositories {"clojars" "http://clojars.org/repo"
                          "central" "http://repo1.maven.org/maven2/"})))
