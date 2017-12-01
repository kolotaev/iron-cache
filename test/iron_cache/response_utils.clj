(ns iron-cache.response-utils
  (:require [iron-cache.core :as ic]
            [cheshire.core :as json]
            [clojure.walk :as walk]))


(defonce valid-server-url (str @#'ic/ROOT_URL "/1"))

(defn response
  "Read response from a file"
  [file-name]
  (slurp (str "resources/responses/" file-name)))

(defn is->map
  "Input Stream to map witk all keywordized keys converter."
  [is]
  (-> is
      slurp
      json/parse-string
      walk/keywordize-keys))

;; Mock Routes ;;

(def echo-list
  {(str valid-server-url "/amiga/caches")
   (fn [req] {:body (-> {:original-request (assoc req :body (-> req :body is->map))} json/generate-string)})})

(def echo-info
  {(str valid-server-url "/amiga/caches/my-cache-id")
   (fn [req] {:body (-> {:original-request (assoc req :body (-> req :body is->map))} json/generate-string)})})


;;; list ;;;
(def list-200
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body (response "list-200")})})

(def list-200-empty
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body "{}"})})

(def list-401
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 401 :body (response "general-401")})})

(def list-500
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 500 :body (response "general-500")})})


;;;; info ;;;
(def info-200
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "info-200")})})

(def info-200-empty
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 200 :body "[]"})})

(def info-403
  {(str valid-server-url "/amiga/caches/planes.my") (fn [_] {:status 403 :body (response "general-403")})})

(def info-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "general-500")})})

;;;; delete! ;;;
(def delete-cache
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "delete-cache")})})

(def delete-cache-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "general-500")})})


;;;; clear! ;;;
(def clear-cache
  {(str valid-server-url "/amiga/caches/credit-cards/clear") (fn [_] {:status 200 :body (response "clear-cache")})})

(def clear-cache-500
  {(str valid-server-url "/amiga/caches/users/clear") (fn [_] {:status 500 :body (response "general-500")})})


;;;; get ;;;
(def get-key-200
  {(str valid-server-url "/amiga/caches/users/items/john") (fn [_] {:status 200 :body (response "get-key-200")})})

(def get-key-404
  {(str valid-server-url "/amiga/caches/users/items/alice") (fn [_] {:status 404 :body (response "general-404")})})

(def get-key-500
  {(str valid-server-url "/amiga/caches/users/items/john") (fn [_] {:status 500 :body (response "general-500")})})


;;;; del ;;;
(def delete-key
  {(str valid-server-url "/amiga/caches/credit-cards/items/1234") (fn [_] {:status 200 :body (response "delete-key")})})

(def delete-key-500
  {(str valid-server-url "/amiga/caches/users/items/john") (fn [_] {:status 500 :body (response "general-500")})})


;;;; incr ;;;
(def incr-key-201
  {(str valid-server-url "/amiga/caches/credit-cards/items/1234/increment")
   (fn [_] {:status 201 :body (response "incr-key-201")})})

(def incr-key-500
  {(str valid-server-url "/amiga/caches/users/items/john/increment")
   (fn [_] {:status 500 :body (response "general-500")})})


;;;; put ;;;
(def put-key-201-minimal
  {(str valid-server-url "/amiga/caches/credit-cards/items/1234")
   (fn [req]
     (let [body (-> req :body is->map)]
       (if (and
            (map? body)
            (some? (:value body)))
         {:status 201 :body (response "put-key-201")}
         {:status 400 :body (response "general-400")})))})

(def put-key-201-additional
  {(str valid-server-url "/amiga/caches/credit-cards/items/1234")
   (fn [req]
     (let [body (-> req :body is->map)]
       (if (and
            (map? body)
            (some? (:value body))
            (some? (:expires_in body))
            (some? (:replace body)))
         {:status 201 :body (response "put-key-201")}
         {:status 400 :body (response "general-400")})))})

(def put-key-500
  {(str valid-server-url "/amiga/caches/users/items/john")
   (fn [_] {:status 500 :body (response "general-500")})})
