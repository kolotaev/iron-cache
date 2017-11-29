;(ns iron-cache.global-test
;  (:use clj-http.fake)
;  (:refer-clojure :exclude [get list])
;  (:require [clojure.test :refer :all]
;            [iron-cache.global :refer :all]
;            [iron-cache.core :as core]
;            [cheshire.core :as json]))
;
;
;(defonce valid-server-url (str @#'core/ROOT_URL "/1"))
;
;(defonce client (core/new-client {:project "amiga" :token "abcd-asdf-qwer"}))
;
;(def echo
;  {(str valid-server-url "/amiga/caches")
;   (fn [req] {:body (-> {:original-request req} json/generate-string)})})
;
;(def list-200
;  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body "[{\"foo\":123}]"})})
;
;
;;; Tests ;;
;
;(deftest with-client-macro
;  (testing "with-client macro, given a client instance, performs correct requests"
;    (with-fake-routes list-200
;      (let [items (atom [])
;            status (atom 0)
;            _ (with-client client
;                (reset! status (:status (list)))
;                (swap! items conj (-> (list) :msg))
;                (swap! items conj (-> (list) :msg))
;                (swap! items conj (-> (list) :msg)))]
;        (is (= 200 @status))
;        (is (= 3 (count @items))))))
;
;  (testing "with-client macro, given a client instance, uses a client's token"
;    (with-fake-routes echo
;      (with-client client
;        (is (= "abcd-asdf-qwer" (-> (list) :msg :original-request :headers :OAuth))))))
;
;  (testing "with-client macro, given a config, performs correct requests"
;    (with-fake-routes list-200
;      (with-client {:project "amiga" :token "my-token"}
;        (is (= 200 (-> (list) :status)))
;        (is (= 123 (-> (list) :msg first :foo))))))
;
;  (testing "with-client macro, given a config, uses a config's token"
;    (with-fake-routes echo
;      (with-client {:project "amiga" :token "my-token"}
;        (is (= "my-token" (-> (list) :msg :original-request :headers :OAuth)))))))
