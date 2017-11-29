(ns iron-cache.response-utils
  (:require [iron-cache.core :as ic]
            [cheshire.core :as json]))


(defn- response [file-name]
  (slurp (str "test/responses/" file-name)))

(defonce valid-server-url (str @#'ic/ROOT_URL "/1"))


;; Mock Routes ;;

(def echo-list
  {(str valid-server-url "/amiga/caches")
   (fn [req] {:body (-> {:original-request req} json/generate-string)})})

(def echo-info
  {(str valid-server-url "/amiga/caches/my-cache-id")
   (fn [req] {:body (-> {:original-request req} json/generate-string)})})


;;; list ;;;
(def list-200
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body (response "list-200")})})

(def list-200-empty
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body "{}"})})

(def list-401
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 401 :body (response "list-401")})})

(def list-500
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 500 :body (response "list-500")})})


;;;; info ;;;
(def info-200
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "info-200")})})

(def info-200-empty
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 200 :body "[]"})})

(def info-403
  {(str valid-server-url "/amiga/caches/planes.my") (fn [_] {:status 403 :body (response "info-403")})})

(def info-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "info-500")})})

;;;; delete! ;;;
(def delete-cache
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "delete-cache")})})

(def delete-cache-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "delete-cache-500")})})


;;;; clear! ;;;
(def clear-cache
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "clear-cache")})})

(def clear-cache-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "clear-cache-500")})})


;;;; get ;;;
(def get-key-200
  {(str valid-server-url "/amiga/caches/users/items/john") (fn [_] {:status 200 :body (response "get-key-200")})})

(def get-key-404
  {(str valid-server-url "/amiga/caches/users/items/alice") (fn [_] {:status 404 :body (response "get-key-404")})})

(def get-key-500
  {(str valid-server-url "/amiga/caches/users/items/john") (fn [_] {:status 500 :body (response "get-key-500")})})


;;;; del ;;;
(def delete-key
  {(str valid-server-url "/amiga/caches/credit-cards/items/1234") (fn [_] {:status 200 :body (response "delete-key")})})

(def delete-key-500
  {(str valid-server-url "/amiga/caches/users/items/john") (fn [_] {:status 500 :body (response "delete-key-500")})})
