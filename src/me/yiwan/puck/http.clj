(ns me.yiwan.puck.http
  (:require [clojure.java.io :as io]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [GET routes]]
            [compojure.route :refer [not-found files]]
            [mount.core :refer [defstate]]
            [me.yiwan.puck.conf :refer [conf]]))

(defn page-handler
  [n]
  (fn [& args]
    (-> (io/file (:wd conf)
                 (-> conf :dir :root)
                 (-> conf :dir :page)
                 (-> (-> conf :site n)
                     (str ".html"))))))

(defn resolve-path
  ;; take vector of keyword of dir conf-name, check conf.edn
  ;; e.g. [:root :post]
  ;; return a string path
  [v]
  (->> (map #(-> conf :dir %) v)
       (into [(:wd conf)])
       (apply io/file)
       .getPath))

(defn create-site
  []
  (routes (GET "/" [] (page-handler :index))
          (files "/" {:root (resolve-path [:root :page])})
          (files "/posts" {:root (resolve-path [:root :post])})
          (files "/assets" {:root (resolve-path [:asset])})
          (files "/files" {:root (resolve-path [:file])})
          (not-found (page-handler :not-found))))

(defstate http
  :start (run-jetty (create-site) {:join? false :port (:port conf)})
  :stop (.stop http))
