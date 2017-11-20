(ns iron-cache.core
  (:refer-clojure :exclude [get list])
  (:require [iron-cache.protocol :refer :all]
            [clj-http.client :as http-client]))


(def ^:const ROOT_URL "cache-aws-us-east-1.iron.io")


(def ^:const DEFAULTS
  {:scheme "https"
   :host ROOT_URL
   :port 443
   :api_version 1
   :http-options {:client-params {"http.useragent" "iron_cache_clj_client"}
                  :content-type :json
                  :accept :json
                  :as :json
                  :throw-exceptions false
                  :coerce {:as :json}}})


(defrecord Client [http]

  Cache

  (list [this & cbs]
    (http :get (format "caches")))

  (info [this cache & cbs]
    (http :get (format "caches/%s" cache)))

  (delete! [this cache & cbs]
    (http :delete (format "caches/%s" cache)))

  (clear! [this cache & cbs]
    (http :post (format "caches/%s/clear" cache)))

  Key

  (get [this cache key & cbs]
    (http :get (format "caches/%s/items/%s" cache key)))

  (put [this cache key val & cbs]
    (http :put (format "caches/%s/items/%s" cache key) val))

  (incr [this cache key val & cbs]
    (http :post (format "caches/%s/items/%s" cache key) {:amount val}))

  (del [this cache key & cbs]
    (http :delete (format "caches/%s/items/%s" cache key))))


(defn- options-from-env
  "Get token and project name out of environment variables."
  []
  {:project (System/getenv "IRON_CACHE_PROJECT")
   :token (System/getenv "IRON_CACHE_TOKEN")})


(defn- validate-options
  "Validates input options. Throws ex if options are inappropriate for client to work."
  [{:keys [project token] :as config}]
  (when-not project
    (throw (ex-info "A project must be specified." {:given config})))
  (when-not token
    (throw (ex-info "An OAuth2 token must be provided." {:given config})))
  config)


(defn- make-requester
  "Get a prepared clj http-client to make requests to a server."
  [opts]
  (let [scheme (:scheme opts)
        host (:host opts)
        port (:port opts)
        make-uri #(format "%s/%s/%s/" (:api_version opts) (:project opts) %)
        headers {:oauth-token (:token opts), :content-type :json, :accept :json}
        client-params (:http-options opts)]
    (fn [method uri & [payload cbs]]
      (http-client/request {:scheme scheme
                            :server-name host
                            :server-port port
                            :request-method method
                            :uri (make-uri uri)
                            :async? (map? (or cbs nil))
                            :headers headers
                            ; ToDo: make merge
                            ; :coerce {:as :json}
                            ; :client-params client-params
                            :body payload}))))


(defn new-client
  "Creates a new client with a given options.
  Throws exception if can't create one based on given options."
  [config]
  (let [opts (validate-options (merge DEFAULTS (options-from-env) config))
        http (make-requester opts)]
    (->Client http)))
