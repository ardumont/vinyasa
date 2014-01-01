(ns vinyasa.inject)

(defn- namespaced-sym [ns sym]
  (symbol (str ns "/" sym)))

(defn- prefixed-sym [prefix sym]
  (symbol (str prefix sym)))

(defn- inject-single [ns sym f]
  (if-let [fvar (resolve f)]
    (clojure.core/intern
     ns
     (with-meta sym
       (select-keys (meta fvar)
                    [:doc :macro :arglists]))
     (deref fvar))
    (throw (Exception. (str "No function or macro found: " f)))))

(defn- inject-row
  ([to-ns prefix [from-ns & eles]]
     (require from-ns)
     (inject-row to-ns from-ns prefix eles))
  ([to-ns from-ns prefix [ele & eles]]
     (when-not (nil? ele)
       (cond (vector? ele)
             (inject-single to-ns
                            (second ele)
                            (namespaced-sym from-ns (first ele)))

             (symbol? ele)
             (inject-single to-ns
                            (prefixed-sym prefix ele)
                            (namespaced-sym from-ns ele)))
       (recur to-ns from-ns prefix eles))))

(defn inject
  ([to-ns rows]
     (inject to-ns nil rows))
  ([to-ns prefix [row & more]]
     (when-not (nil? row)
       (inject-row to-ns prefix row)
       (recur to-ns prefix more))))
