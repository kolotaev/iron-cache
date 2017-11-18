(ns iron-cache.http
  (:require [clj-http.client :as http-client]
            [clojure.string :as str]))


(defn make-requester
  "Get a prepared clj http-client to make requests to a server."
  [opts]
  (let [url (str (str/replace (:host opts) #"/$" "") ":" (:port opts))
        resource #(format "%s/%s" url %)
        scheme (:scheme opts)
        port (:port opts)
        headers {:oauth-token (:token opts) :content-type :json :accept :json}
        client-params {:http.useragent (:user_agent opts) :socket-timeout 1000 :conn-timeout 1000}]
    (fn [method uri & [payload cbs]]
      (http-client/request {:scheme scheme
                            ; :server-name "localhost"
                            :server-port port
                            :request-method :get
                            :uri (resource uri)
                            :async? (map? (or cbs nil))
                            :headers headers
                            :coerce {:as :json}
                            :client-params client-params
                            :body payload}))))
