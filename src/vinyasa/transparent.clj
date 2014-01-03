(ns vinyasa.transparent)

(def ^:dynamic *cache* (atom {}))

(defn conj-fn [x]
  (fn [v]
    (if-not v [x] (conj v x))))

(defn classify-method [method acc]
  (let [name   (.getName method)
        type   (if (java.lang.reflect.Modifier/isStatic
                    (.getModifiers method))
                 :static
                 :method)
        params (.getParameterTypes method)
        arity  (count params)]
    (.setAccessible method true)
    (-> acc
        (update-in [:name name type arity]
                   (conj-fn method))
        (update-in [:type name type arity]
                   (conj-fn method)))))

(defn classify-field [field acc]
  (let [name (.getName field)]
    (.setAccessible field true)
    (-> acc
        (assoc-in [:name name :field] field)
        (assoc-in [:type :field name] field))))

(defn classify-constructor [constructor acc]
  (let [params (.getParameterTypes constructor)
        arity  (count params)]
    (.setAccessible constructor true)
    (-> acc
        (update-in [:type :constructor arity]
                   (conj-fn constructor)))))

(defn classify-methods [[method & more] acc]
  (if (nil? method) acc
      (recur more (classify-method method acc))))

(defn classify-fields [[field & more] acc]
  (if (nil? field) acc
      (let [name (.getName field)]
        (recur more (assoc-in acc [name :field] field)))))

(defn classify-constructors [[constructor & more] acc]
  (if (nil? constructor) acc
      (recur more (classify-constructor constructor acc))))

(defn classify [cls]
  (let [methods (.getDeclaredMethods cls)
        fields  (.getDeclaredFields cls)
        constructors (.getDeclaredConstructors cls)]
    (->> (assoc {} :class (.getName cls))
         (classify-methods (seq methods))
         (classify-fields (seq fields))
         (classify-constructors (seq constructors)))))

(declare invoke-tp)

(defn make-invoke-tp-form [args]
  (clojure.walk/postwalk
   (fn [x]
     (cond (and (list? x)
                (= 'invoke-tp (first x)))
           (concat x args)

           (vector? x)
           (vec (concat x args))
           :else x))
   '(invoke [self call]
            (invoke-tp obj classified call))))

(defmacro make-transparent-type [n]
  (concat
   '(deftype Transparent [obj classified]
      clojure.lang.IFn
      (invoke [self] (invoke-tp obj classified)))

   (map make-invoke-tp-form
        (for [l (range n)]
          (vec (for [x (range l)]
                 (symbol (str "arg" x))))))))

(make-transparent-type 20)

(defn transparent
  ([obj] (transparent obj true))
  ([obj use-cache]
     (let [cls (class obj)
           clsname (.getName cls)
           classified (or (and use-cache
                               (get @*cache* clsname))
                          (let [res (classify (class obj))]
                            (swap! *cache* assoc clsname res)
                            res))]
       (Transparent. obj classified))))

(defn get-valid [methods]
;;  (println methods)
  (first methods))

(defn invoke-tp
  ([obj classified] obj)
  ([obj classified call & args]
     (cond (= :new call)
           (str "New constructor for " (:class classified))

           :else
           (let [blk (-> (:name classified) (get (name call)))
                 _ (println blk)]
             (cond (:field blk)
                   (.get (:field blk) obj)

                   (:method blk)
                   (let [arity (count args)
                         method (-> (:method blk) (get arity) (get-valid))]
                     (.invoke method obj (into-array Object args)))
                   :else
                   (str "NOTHING FOUND FOR " call))))))


((Transparent. "Oeuoeu" (classify String)))
((transparent (java.util.Date.)) "getTimeImpl")

(keys (get-in (get @*cache* "java.util.Date") [:name]))
(keys (get @*cache* "java.util.Date"))


(comment

  (classify Object)

  (proxy [clojure.lang.IFn] []
    (invoke [& more]
      more))

  (>pprint
   (first (filter (fn [method] (= (.getName method) "wait"))
                  (seq (.getMethods java.sql.Date)))))

  (def obj (java.sql.Date. 0))
  (def cls (class obj))
  (def mthd (.getDeclaredMethod cls "toString" (into-array Class [])))
  (.setAccessible mthd true)
  (.invoke mthd obj (into-array Class []))
  (.isAccessible mthd)


  (def mthd (first (seq (.getConstructors java.sql.Date))))
  (def mthd (first (seq (.getMethods java.sql.Date))))
  (.setAccessible mthd false)
  (.invoke mthd (java.sql.Date. 0))
  (count (.getParameterTypes mthd))
  (.getDeclaringClass mthd)

  ()
  (java.lang.reflect.Modifier/isFinal (.getModifiers mthd))

  (invoke-constructor)
)
