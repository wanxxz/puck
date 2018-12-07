(ns me.yiwan.puck.conf
  (:require [cprop.core :refer [load-config]]
            [mount.core :refer [defstate args]]
            [environ.core :refer [env]]))

(defstate conf :start (let [c (load-config :resource "conf.edn")]
                        (if (env :dev)
                          (assoc c :wd "test")
                          (assoc c :wd (:working-directory (args))))))
