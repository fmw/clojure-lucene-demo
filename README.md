Clojure Lucene Demo
===================

The original reason for writing this code was to create a quick
proof-of-concept demonstrating a problem I was having with filters in Lucene.
When I added a filter my query returned zero results. As [Uwe Schindler][uwe]
pointed out this happened because my code told Lucene to analyze the field,
while filters expect a literal value unless you customize them with an
analyzer on the query side. Thanks, Uwe!

Now I took the time to write this abstract example of how I use Lucene with
Clojure, I'm planning to keep it up as reference for others. That way, the
code will serve some purpose now the problem is solved. Of course, any errors
or non-idiomatic usage should be fixed by that point. Keep in mind that this
is the first Clojure code I'm releasing, only two weeks after first picking up
the language, so there might be a few non-idiomatic gotchas lurking around!

Running the code is easy (assuming you have [Leiningen][lein] installed):

    git clone https://github.com/fmw/clojure-lucene-demo.git
    cd clojure-lucene-demo
    lein test

Check [Mark Triggs' Mailindex repository][mailindex] for another example of
Lucene code in Clojure. Also, make sure to look at the [unit
tests][test-search], because they provide the best documentation to the code.
Feel free to contact me using the email address below in case you have any
questions or suggestions.

[uwe]: http://www.thetaphi.de/

[test-search]: https://github.com/fmw/clojure-lucene-demo/blob/master/test/clojure_lucene_demo/test/core.clj

[lein]: https://github.com/technomancy/leiningen

[mailindex]: https://github.com/marktriggs/mailindex

Copyright 2011, F.M. (Filip) de Waard <<fmw@vix.io>>.
Distributed under the Apache License, version 2 (see the LICENSE file).
