(ns vinyasa.pull
  (:require [cemerick.pomegranate])
  (:refer-clojure :exclude [pull]))

(defmacro pull
  ([lib] (list `pull lib "RELEASE"))
  ([lib release]
     (list `cemerick.pomegranate/add-dependencies
           :coordinates [[(list `symbol (str lib)) release]]
           :repositories {"clojars" "http://clojars.org/repo"
                          "central" "http://repo1.maven.org/maven2/"})))
