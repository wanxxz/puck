(ns me.yiwan.puck.html
  (:require [me.yiwan.puck.markdown
             :refer
             [generate-blocks meta-map meta-seq parse-content parse-meta]]
            [me.yiwan.puck.template :refer :all]))

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
  (let [s (symbol (str "template-" n))
        t (ns-resolve 'me.yiwan.puck.template s)]
    (if (nil? t)
      (println (format "template not found: %s" n))
      t)))

(defn generate-html
  [txt]
  (let [meta (parse-meta txt)]
    (if (some? meta)
      (let [content (-> txt parse-content generate-blocks)
            template-function (some-> meta resolve-template-name resolve-template-function)]
        (if (some? template-function)
          (let [meta @(-> meta meta-seq meta-map)]
            (reduce str (template-function meta content))))))))

