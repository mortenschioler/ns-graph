(ns ns-graph.impl
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
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
  (when-let [start (.lastIndexOf f (int \. ))]
    (subs f (inc start))))

(defn resolve-opts
  [{:keys [source-paths f fmt]}]
  (let [fmt (keyword (or fmt
                         (and f (file-ending f))
                         :pdf))
        f (or f (format "ns-graph.%s" (name fmt)))
        source-paths (or source-paths ["src"])]
    {:fmt fmt
     :f f
     :source-paths source-paths}))

(defn graph
  [{:keys [source-paths]}]
  (->> source-paths
       (map io/file)
       ns-find/find-ns-decls
       (map (juxt ns-parse/name-from-ns-decl ns-parse/deps-from-ns-decl))
       (into {})))

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
