> I've written that language to make you awesome.
> > -- the author


PHP.reboot is a reboot of PHP,
each Hollywood movie has its own [reboot](http://www.imdb.com/title/tt0796366/),
why not doing the same for one of the most popular programming language.
The aim is to keep the philosophy of PHP
but adapt it to be more in sync with the Web of 2010.

Quick links:
  * [Mailing list](http://groups.google.com/group/phpreboot)
  * [Download](http://code.google.com/p/phpreboot/downloads/list)


Highlights:
  * less $, less ';' like in javascript
  * secure by default: no eval, no magic quotes/string interpolation
  * full unicode support
  * no from/to string auto-conversion
  * XML literal
  * JSON literal
  * a SQL compatible syntax
  * XPath/XQuery compatible syntax
  * Perl 5 regex literal
  * URI/file literal
  * a dynamic language with duck typing and a gradual type system
  * fast as Java thanks to a runtime profiler/optimizer and JSR 292 API
  * provide an embedded database [derby](http://db.apache.org/derby/) and a standalone web server [grizzly](https://grizzly.dev.java.net/) by default
  * can run on top of any JEE stacks, or android, it's a JVM language !

PHP.reboot works this latest beta release of [jdk7](http://download.java.net/jdk7/binaries/). Or with any java6 jdks thanks to the [jsr292-backport](http://code.google.com/p/jvm-language-runtime/).



But because some samples are better than long a speech:

  * native XML syntax
```
<html>
 <title>hello PHP.reboot</title>
 <body>
   <h1>
    {
      echo "Hello World !"
    }
   </h1>
 </body>
</html>
```

  * Language as close as possible to PHP
```
function fibo(a) {
  if (a == 0)
    return 1
  elseif (a == 1)
    return 1
  else
    return fibo(a - 1) + fibo(a - 2)
}

echo fibo(7)
```

  * native SQL syntax
```
create table foo (
  id integer not null,
  name varchar(64),
  primary key(id)
)
insert into foo (id, name) values (4, 'hooooooa');
```

  * a SQL query can use any variables
```
n = 3
a = select name from foo where id < $n
foreach(a as key: value) {
  echo "row: " + key + " name: " + value.name
}
```

  * XML and SQL can be mixed together
```
<html>
 <body>
  <ol>
   {
     resultset = select * from foo where name = 'bar'
     foreach(resultset as value)
     {
       echo <li>$(value.name)</li>
     }
   }
  </ol>
 </body>
</html>
```

  * native JSON syntax
```
persons = [
 {
  "name": "Smith",
  "age": 20,
  "color": "blue" 
 },
 {
  "name": "Wesson",
  "age": 17,
  "color": "red" 
 }
]
 
foreach(persons as person) {
  echo "Name:" + person.name
  echo "Color:" + person.color
}
```

  * native URI (here http) and XQuery
```
result = document(http://search.twitter.com/search.atom?lang=en&q=Java)

titles = for node in result//entry/title
           return node  
           
<news>
 {
   foreach(titles as title) {
     title.name = "description"
     echo title
   }
 }
</news>
```

  * it also quacks like a [duck](http://en.wikipedia.org/wiki/Duck_typing)
```
ducks = []
add(ducks, "quack")   // non empty string
add(ducks, [])        // empty array

foreach(ducks as duck) {
  echo isEmpty(duck)
}         
    
// prints false
// prints true
```

More [samples ...](http://code.google.com/p/phpreboot/source/browse/#svn/trunk/phpreboot/test)


The main grammar is [available here](http://code.google.com/p/phpreboot/source/browse/trunk/phpreboot/phpreboot.ebnf) and the [SQL DSL](http://code.google.com/p/phpreboot/source/browse/trunk/phpreboot/sql.ebnf) is available here.


What's missing ?
  * contributors
  * embedded syntax for Path/URI and XQuery
  * an eclipse/netbeans plugin
  * ports of all useful PHP functions