(ns iron-cache.protocol
  (:refer-clojure :exclude [get list]))


(defprotocol Cache
  "Iron cache instance manipulation"
  (list [this & cbs] "Get a list off all caches")
  (info [this cache & cbs] "Get information about a cache")
  (delete! [this cache & cbs] "Delete a cache")
  (clear! [this cache & cbs] "Clear a cache"))

(defprotocol Key
  "Iron cache instance keys manipulation"
  (get [this cache key & cbs] "Get a value stored in a key from a cache")
  (put [this cache key val & cbs] "Add key/value pair to a cache")
  (incr [this cache key val & cbs] "Increment value in a cache stored at key by a specified amount")
  (del [this cache key & cbs] "Delete a value from a cache stored at key"))
