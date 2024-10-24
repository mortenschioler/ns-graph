(ns ns-graph.impl
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.namespace.find :as ns-find]
    [clojure.tools.namespace.parse :as ns-parse]
    [dorothy.core :as dot]
    [dorothy.jvm :as dot-jvm]))

(defn edges
  [[dependant dependencies]]
  (let [a (keyword dependant)]
    (conj (map vector (repeat a) (map keyword dependencies))
          a)))

(defn file-ending
  [^String f]
  (when-let [start (.lastIndexOf f (int \.))]
    (subs f (inc start))))

(defn prefix?-fn
  [prefix]
  (fn [s] (str/starts-with? s prefix)))

(defn compile-fn
  [form]
  (assert (list? form))
  (assert (#{'fn} (first form)))
  (eval form))


(defn resolve-opts
  [{:keys [source-paths f fmt prefix exclude]}]
  (let [fmt (keyword (or fmt
                         (and f (file-ending f))
                         :pdf))
        f (or f (format "ns-graph.%s" (name fmt)))
        source-paths (or source-paths ["src"])]
    (cond->
      {:fmt fmt
       :f f
       :source-paths source-paths}
      prefix (assoc :prefix prefix)
      exclude (assoc :exclude exclude)
      (or prefix exclude)
      (assoc :filter-compiled (apply every-pred
                                     (remove nil? [(some-> prefix prefix?-fn)
                                                   (some-> exclude complement)]))))))

(defn filter-graph
  [opts graph]
  (if-let [pred (:filter-compiled opts)]
    (reduce
      (fn [acc [k v]]
        (if-not (pred k)
          acc
          (assoc acc k (into #{} (filter pred) v))))
      {}
      graph)
    (into {} graph)))

(defn graph
  [{:keys [source-paths] :as opts}]
  (->> source-paths
       (map io/file)
       ns-find/find-ns-decls
       (map (juxt ns-parse/name-from-ns-decl ns-parse/deps-from-ns-decl))
       (filter-graph opts)))

(defn dot-graph
  [graph]
  (->> graph
       (mapcat edges)
       (dot/digraph)
       (dot/dot)))

(defn show!
  [dotted-graph]
  (->> dotted-graph
       (graph)
       (dot-graph)
       (dot-jvm/show!)))

(defn save!
  [dotted-graph {:keys [f fmt]}]
  (dot-jvm/save! dotted-graph f {:format fmt}))

(defn stats
  [graph]
  (->>
    (reduce-kv
      (fn [acc dependant dependencies]
        (as-> acc acc
              (assoc-in acc [dependant :dependencies] (count dependencies))
              (reduce (fn [acc dependency] (update-in acc [dependency :dependants] (fnil inc 0)))
                      acc
                      dependencies)))
      {}
      graph)
    (sort-by (comp (juxt :dependants :dependencies) val))
    reverse))
