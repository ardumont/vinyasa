(ns vinyasa.intern
  (:refer-clojure :exclude [intern]))

(defn intern-fn [ns sym f]
  (if-let [fvar (resolve f)]
    (clojure.core/intern
     ns
     (with-meta sym
       (select-keys (meta fvar)
                    [:doc :macro :arglists]))
     (deref fvar))
    (throw (Exception. (str "No function or macro found: " f)))))

(defn ns-str [f]
  (let [fstr (str f)
        [[_ nstr v]] (re-seq #"([^/]+)/([^/]+)" fstr)]
    nstr))

(defmacro intern
  ([sym f] (list `intern sym f nil))
  ([sym f require?]
     (list `do
           (if (= require? :require)
             (list `require (list `symbol (ns-str f))))
           (list `intern-fn
                 (list `symbol (str "clojure.core"))
                 (list `symbol (str sym))
                 (list `symbol (str f))))))
