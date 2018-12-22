puck
====

name from [Puck(パック Pakku)](https://en.wikipedia.org/wiki/List_of_Berserk_characters#Puck)

What is this
------------
A ~~Clojure toy project~~ useful(to me), as-simple-as-posibble markdown blogging tool.

Features
--------
- Markdown support
- Templates
- Snippets
- Built-in HTTP server
- No Cookies/Sessions support (wat? yeah, it's true, you don't need these things for a weblog)

Things may have
---------------
- RSS
- Posts pagination
- HTTPS

Get start
---------
- `clojure -A:main -w path init`
- `clojure -A:main -w path start`
- check `http://localhost:8080` or `http://localhost:8080/posts/2018-11-01-foo-bar.html`

Directory
---------
```
----
   |
   |-- assets       css, font, favicon etc.
   |
   |-- conf.edn     configration
   |
   |-- files        zip, tar.gz etc.
   |
   |-- pages        .md files, e.g. home.md 404.md
   |
   |-- posts        .md files, e.g. 2018-11-01-foo-bar.md
   |
   |-- snippets     .html files, e.g. head.html post-list.html
   |
   |-- templates    .html files, e.g. post.html page.html
   |
   |-- www          HTTP server root
     |
     |-- pages      .html files, pages/home.md > pages/home.html
     |
     |-- posts      .html files, posts/2018-11-01-foo-bar.md > posts/2018-11-01-foo-bar.html
```

URL
---
- `/posts`          posts, e.g. `/posts/2018-11-01-foo-bar.html` by `dir/www/posts/2018-11-01-foo-bar.html`
- `/`               index, by `dir/www/pages/home.html`
- `/404.html`       not-found page, by `dir/www/pages/404.html`

CLI
---
- `clojure -A:main -h`

Credits
-------
- [Hitman](https://github.com/chameco/Hitman) Samuel Breese
