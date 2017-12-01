(ns iron-cache.util)


(defn env
  "System/getenv wrapper"
  [key]
  (System/getenv key))


(defn deep-merge
  "Deeply merges maps so that nested maps are combined rather than replaced."
  [& vs]
  (if (every? map? vs)
    (apply merge-with deep-merge vs)
    (last vs)))


(defn map-subset?
  "Helper function to test if map is a subset of another map"
  [a b]
  (every? (fn [[k _ :as entry]] (= entry (find b k))) a))


(defn format-str
  "Like a regular `format`, but formats keywords to strings correctly: (:foo -> 'foo')"
  [fmt & rest]
  (->> rest
       (map #(if (instance? clojure.lang.Named %)
               (name %)
               %))
       (apply format fmt)))
