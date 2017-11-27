(ns iron-cache.client-test
  (:use clj-http.fake)
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]
            [cheshire.core :as json]))


(def wait-ms 5000)

(defn- response [file-name]
  (slurp (str "test/responses/" file-name)))

(defonce client (ic/new-client {:project "amiga" :token "abcd-asdf-qwer"}))

(defonce valid-server-url (str @#'ic/ROOT_URL "/1"))


;; Mock Routes ;;

(def echo-list
  {(str valid-server-url "/amiga/caches")
   (fn [req] {:body (-> {:original-request req} json/generate-string)})})

(def echo-info
  {(str valid-server-url "/amiga/caches/my-cache-id")
   (fn [req] {:body (-> {:original-request req} json/generate-string)})})

(def list-200
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body (response "list-200")})})

(def list-200-empty
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body "[]"})})

(def list-401
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 401 :body (response "list-401")})})

(def list-500
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 500 :body (response "list-500")})})


;; Tests ;;

(deftest request-sanity
  (testing "Iron-cache client has only :status and :msg fields"
    (with-fake-routes list-200
      (let [resp (ic/list client)]
        (is (= [:status :msg] (-> resp keys))))))

  (testing "oauth2 token was sent correctly"
    (with-fake-routes echo-list
      (let [headers (-> client ic/list :msg :original-request :headers)]
        (is (contains? headers :OAuth))
        (is (= "abcd-asdf-qwer" (:OAuth headers))))))

  (testing "content-type header is json"
    (with-fake-routes echo-list
      (let [headers (-> client ic/list :msg :original-request :headers)]
        (is (contains? headers :content-type))
        (is (= "application/json" (:content-type headers))))))

  (testing "accept header is json"
    (with-fake-routes echo-list
      (let [headers (-> client ic/list :msg :original-request :headers)]
        (is (contains? headers :accept))
        (is (= "application/json" (:accept headers))))))

  (testing "cache id (string, keyword, symbol) is correctly farmatted in a request URI"
    (with-fake-routes echo-info
      (are [id] (= "/1/amiga/caches/my-cache-id" (-> client (ic/info id) :msg :original-request :uri))
        "my-cache-id"
        :my-cache-id
        'my-cache-id)))

  (testing "project id (string, keyword, symbol) is correctly farmatted in a request URI"
    (with-fake-routes echo-list
      (are [project-id](= "/1/amiga/caches" (-> {:project project-id :token "abc"}
                                                ic/new-client
                                                ic/list
                                                :msg :original-request :uri))
        "amiga"
        :amiga
        'amiga))))


(deftest cache-list
  (testing "correct list of caches"
    (with-fake-routes list-200
      (let [resp (ic/list client)]
        (is (= 200 (-> resp :status)))
        (is (= 2 (-> resp :msg count)))
        (is (= "amiga" (-> resp :msg first :project_id)))
        (is (= "b" (-> resp :msg last :name))))))

  (testing "empty list of caches"
    (with-fake-routes list-200-empty
      (let [resp (ic/list client)]
        (is (= 200 (-> resp :status)))
        (is (= 0 (-> resp :msg count)))
        (is (= nil (-> resp :msg first :project_id))))))

  (testing "non-authorized request"
    (with-fake-routes list-401
      (let [resp (ic/list client)]
        (is (= 401 (-> resp :status)))
        (is (= "You must be authorized" (-> resp :msg))))))

  (testing "server went down"
    (with-fake-routes list-500
      (let [resp (ic/list client)]
        (is (= 500 (-> resp :status)))
        (is (= "Iron Server went down" (-> resp :msg)))))))


;(deftest cache-list-async
;  (testing "correct list of caches"
;    (with-fake-routes list-200
;      (let [result (promise)
;            _ (ic/list client {:ok #(deliver result %)
;                               :fail #(deliver result %)})
;            resp (deref result wait-ms :timeout)]
;        (is (some? resp))
;        (is (= 200 (:status resp)))
;        (is (= 2 (-> resp :msg count)))
;        (is (= "amiga" (-> resp :msg first :project_id)))
;        (is (= "b" (-> resp :msg last :name))))))

;    (testing "empty list of caches"
;      (with-fake-routes list-200-empty
;        (let [resp (ic/list client)]
;          (is (= 200 (-> resp :status)))
;          (is (= 0 (-> resp :msg count)))
;          (is (= nil (-> resp :msg first :project_id))))))
;
;    (testing "non-authorized request"
;      (with-fake-routes list-401
;        (let [resp (ic/list client)]
;          (is (= 401 (-> resp :status)))
;          (is (= "You must be authorized" (-> resp :msg))))))
;
;    (testing "server went down"
;      (with-fake-routes list-500
;        (let [resp (ic/list client)]
;          (is (= 500 (-> resp :status)))
;          (is (= "Iron Server went down" (-> resp :msg))))))
;  )
