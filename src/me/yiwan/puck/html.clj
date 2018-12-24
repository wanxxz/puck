(ns me.yiwan.puck.html
  (:require [mount.core :refer [defstate]]
            [me.yiwan.puck.template :refer [template]]
            [me.yiwan.puck.markdown
             :refer
             [generate-blocks meta-map meta-seq parse-content parse-meta]]))

(defn resolve-template-name
  "ignore duplicate, take last one"
  [meta]
  (let [meta (meta-seq meta)
        res (filter #(= (-> % keys first) :template) meta)]
    (if (empty? res)
      (println (format "meta not found: %s" :template))
      (-> (last res) vals first))))

(defn resolve-template-function
  [n]
  (let [f ((keyword n) template)]
    (if (nil? f)
      (println (format "template not found: %s" n))
      f)))

(defstate generate-html :start
  (fn
    [txt]
    (let [meta (parse-meta txt)]
      (if (some? meta)
        (let [content (-> txt parse-content generate-blocks)
              template-function (some-> meta resolve-template-name resolve-template-function)]
          (if (some? template-function)
            (let [meta (-> meta meta-seq meta-map)]
              (reduce str (template-function meta content)))))))))
