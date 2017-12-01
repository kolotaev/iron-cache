(ns iron-cache.global
  (:refer-clojure :exclude [get list])
  (:require [iron-cache.core :as core]))


;;; Client global instance and methods ;;;

(def ^:dynamic *client* nil)


;;; Cache and Key protocols proxies ;;;

(defn list
  "Get a list off all caches using global client"
  [& [cbs]]
  (core/list *client* cbs))

(defn info
  "Get information about a cache using global client"
  [cache & [cbs]]
  (core/info *client* cache cbs))

(defn delete!
  "Delete a cache using global client"
  [cache & [cbs]]
  (core/delete! *client* cache cbs))

(defn clear!
  "Clear a cache using global client"
  [cache & [cbs]]
  (core/clear! *client* cache cbs))

(defn get
  "Get a value stored in a key from a cache using global client"
  [cache key & [cbs]]
  (core/get *client* cache key cbs))

(defn put
  "Add key/value pair to a cache using global client"
  [cache key val & [cbs]]
  (core/put *client* cache key val cbs))

(defn incr
  "Increment value in a cache stored at key by a specified amount using global client"
  [cache key val & [cbs]]
  (core/incr *client* cache key val cbs))

(defn del
  "Delete a value from a cache stored at key using global client"
  [cache key & [cbs]]
  (core/del *client* cache key cbs))


;;; Client initialization ;;;

(defn init-client!
  "Instantiates a global client instance"
  [config]
  (alter-var-root #'*client*
                  (constantly (core/new-client config))))


(defmacro with-client
  "Allows you to call Iron Cache functions with a specific client.
  You can pass a client instance or a configuration map that will instantiate a client."
  [x & body]
  `(let [client# (if (record? ~x)
                   ~x
                   (core/new-client ~x))]
     (binding [*client* client#]
       ~@body)))
