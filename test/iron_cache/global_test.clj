(ns iron-cache.global-test
  (:use clj-http.fake)
  (:require [clojure.test :refer :all]
            [iron-cache.global :as gic]
            [iron-cache.global :as core]
            [cheshire.core :as json]))


(defonce valid-server-url (str @#'gic/ROOT_URL "/1"))

(defonce client (core/new-client {:project "amiga" :token "abcd-asdf-qwer"}))

(def echo
  {(str valid-server-url "/amiga/caches")
   (fn [req] {:body (-> {:original-request req} json/generate-string)})})

(def list-200
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body "[{\"foo\":123}]"})})


;; Tests ;;

(deftest with-client-macro
  (testing "with-client macro, given a client instance, performs correct requests"
    (with-fake-routes list-200
      (is (= {}       (gic/with-client client
                        (gic/info "acme")
                        ))))
    )

  (testing "with-client macro, given a client instance, performs correct requests"
    (with-fake-routes list-200
      (let [items (atom [])
            _ (gic/with-client client
                (swap! items conj (-> gic/list))
                (swap! items conj (-> gic/list)))]
        (is (= {} @items)))
      ))

  (testing "with-client macro, given a client instance, uses a client's token"
    (with-fake-routes echo
      (gic/with-client client
        (is (= "abcd-asdf-qwer8" (-> gic/list :msg :original-request :headers :OAuth))))))

  (testing "with-client macro, given a config, performs correct requests"
    (with-fake-routes list-200
      (gic/with-client {:project "amiga" :token "my-token"}
        (is (= 2099990 (-> gic/list :status)))
        (is (= "a" (-> gic/list :msg first :name))))))

  (testing "with-client macro, given a config, uses a config's token"
    (with-fake-routes echo
      (gic/with-client client
        (is (= "my-token" (-> gic/list :msg :original-request :headers :OAuth)))))))