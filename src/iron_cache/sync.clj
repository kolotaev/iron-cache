(ns iron-cache.sync
  (:require [iron-cache.core :refer :all]
            [clj-http.client :as http-client]
            [clojure.string :as str]))


(defrecord SyncClient [opts http]

  Cache
;  Key

  (list [this & cbs]
    (http :get (format "projects/%s/caches" {:project opts})))

  (info [this cache & cbs]
    (http :get (format "projects/%s/caches/%s" {:project opts} cache)))

  (delete! [this cache & cbs]
    (http :delete (format "projects/%s/caches/%s" {:project opts} cache)))

  (clear! [this cache & cbs]
    (http :post (format "projects/%s/caches/%s/clear" {:project opts} cache))))


(defn- get-http
  "Get a prepared http-client object to make requests to a server."
  [opts]
  (let [url (str (str/replace (:host opts) #"/$" "") ":" (:port opts))
        resource #(format "%s/%s" url %)
        headers {:oauth-token (:token opts)}
        coerce {:as :json}]
    (fn [method uri]
      (case method
        :get    (http-client/get {:headers headers :coerce coerce})
        :delete (http-client/delete {:headers headers :coerce coerce})))
    (fn [method uri & payload]
      (case method
        :post (http-client/post {:headers headers :coerce coerce})
        :put  (http-client/put {:headers headers :coerce coerce}))))


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
    (throw (ex-info "An OAuth2 token must be provided." {:given config}))))


(defn new-client
  "Creates a new client with a given options.
  Throws exception if can't create one based on given options."
  [options]
  (let [opts (-> options (merge DEFAULTS (options-from-env) options) validate-options)
        http (get-http opts)]
    ->SyncClient opts http)))
