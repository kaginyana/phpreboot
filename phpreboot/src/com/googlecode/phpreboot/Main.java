package com.googlecode.phpreboot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;

import com.googlecode.phpreboot.doc.Doclet;
import com.googlecode.phpreboot.interpreter.Analyzer;
import com.googlecode.phpreboot.interpreter.Echoer;
import com.googlecode.phpreboot.interpreter.RootScope;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.sql.GenericSQLConnection;
import com.googlecode.phpreboot.webserver.WebScriptDispatcher;
import com.sun.grizzly.http.embed.GrizzlyWebServer;

// Warning ! every modifications in this class must be backported into Main16
public class Main {
  enum Option {
    verbose(": turn on verbose mode"),
    webserver(": start a web server"),
    aot(": Ahead Of Time compiler"),
    doc(": start doc") {
      @Override
      boolean parse(EnumMap<Option, Object> optionMap, Iterator<String> it) {
        ArrayList<String> arguments = new ArrayList<>();
        while(it.hasNext())
          arguments.add(it.next());
        optionMap.put(this, arguments.toArray(new String[arguments.size()]));
        return true;
      }
    },
    db("[db name] : specify the database name") {
      @Override
      boolean parse(EnumMap<Option, Object> optionMap, Iterator<String> it) {
        if (!it.hasNext()) {
          System.err.println("option -db must be folowed by a directory name");
          return false;
        }
        optionMap.put(this, it.next());
        return true;
      }
    },
    /*driverName("[driver name] : specify the jdbc driver name") {
      @Override
      boolean parse(EnumMap<Option, Object> optionMap, Iterator<String> it) {
        if (!it.hasNext()) {
          System.err.println("option -drivername must be folowed by a name of a JDBC driver");
          return false;
        }
        optionMap.put(this, it.next());
        return false;
      }
    },*/
    jdbcScheme("[jdbc scheme] : jdbc protocole scheme") {
      @Override
      boolean parse(EnumMap<Option, Object> optionMap, Iterator<String> it) {
        if (!it.hasNext()) {
          System.err.println("option -jdbcScheme must be folowed by a protocol scheme");
          return false;
        }
        optionMap.put(this, it.next());
        return false;
      }
    },
    documentRoot("[document root] : root directory of the files served by the web server") {
      @Override
      boolean parse(EnumMap<Option, Object> optionMap, Iterator<String> it) {
        if (!it.hasNext()) {
          System.err.println("option -documentRoot must be folowed by a directory name");
          return false;
        }
        optionMap.put(this, it.next());
        return false;
      }
    }
    ;

    private final String help;
    private Option(String help) {
      this.help = help;
    }
    
    String help() {
      return help;
    }
    
    boolean parse(EnumMap<Option, Object> optionMap, @SuppressWarnings("unused") Iterator<String> it) {
      optionMap.put(this, null);
      return true;
    }
  }
  
  private static String trimExtension(Path path) {
    String name = path.getFileName().toString();
    int index = name.lastIndexOf('.');
    if (index<1)
      return name;
    return name.substring(0, index);
  }

  public static void main(String[] args) throws IOException {
    EnumMap<Option,Object> optionMap = new EnumMap<>(Option.class);

    ArrayList<Path> filePaths = new ArrayList<>(); 
    if (args.length != 0) {
      Iterator<String> it = Arrays.asList(args).iterator();
      while(it.hasNext()) {
        String arg = it.next();
        if (arg.length()>1 && arg.charAt(0)=='-') {
          Option option;
          try {
            option = Option.valueOf(arg.substring(1));
          } catch (IllegalArgumentException e) {
            printHelp();
            return;
          }

          if (!option.parse(optionMap, it)) {
            printHelp();
            return;
          }

        } else {
          filePaths.add(Paths.get(arg));
        }
      }
    }

    // common inits

    //String driverName = get(optionMap, Option.driverName, String.class, "org.apache.derby.jdbc.EmbeddedDriver");
    String dbName = get(optionMap, Option.db, String.class, "phprDB");
    String protocolScheme = get(optionMap, Option.jdbcScheme, String.class, "jdbc:derby");
    //Class.forName(driverName);
    String jdbcURL = protocolScheme + ":" + dbName + ";create=true";

    boolean verbose = true /*optionMap.containsKey(Option.verbose)*/;
    
    if (optionMap.containsKey(Option.webserver)) {
      GrizzlyWebServer ws = new GrizzlyWebServer(8080);
      Path rootPath = Paths.get(get(optionMap, Option.documentRoot, String.class, "www"));
      WebScriptDispatcher webDispatcher = new WebScriptDispatcher(rootPath, jdbcURL);
      ws.addGrizzlyAdapter(webDispatcher, new String[]{"/"});
      ws.start();
      return;
    }
    
    if (optionMap.containsKey(Option.doc)) {
      ArrayList<String> arguments = new ArrayList<>();
      Collections.addAll(arguments, "-doclet", Doclet.class.getName());
      Collections.addAll(arguments, (String[])optionMap.get(Option.doc));
      com.sun.tools.javadoc.Main.main(arguments.toArray(new String[arguments.size()]));
      return;
    }
    
    InputStream input;
    String scriptName;
    if (filePaths.isEmpty()) {
      input = System.in;
      scriptName = "script";
    } else {
      Path path = filePaths.get(0);
      scriptName = trimExtension(path);
      input = Files.newInputStream(path);
    }

    InputStreamReader reader = new InputStreamReader(input);
    PrintWriter writer = new PrintWriter(System.out);

    GenericSQLConnection sqlConnection = new GenericSQLConnection(jdbcURL);
    RootScope rootScope = new RootScope();
    rootScope.register(new Var("SQL_CONNECTION", true, true, PrimitiveType.ANY, sqlConnection));
    
    try {
      if (optionMap.containsKey(Option.aot)) {
        Analyzer.compileAheadOfTime(scriptName, reader, rootScope);
      } else {
        Analyzer.interpret(reader, Echoer.writerEchoer(writer), rootScope);  
      }
    } catch(Throwable t) {
      if (verbose)
        t.printStackTrace(System.err);
      else
        System.err.println("error: " + t.getMessage());
    } finally {
      sqlConnection.close();
    }
  }

  private static <T> T get(EnumMap<Option,Object> map, Option option, Class<T> type, T defaultValue) {
    if (map.containsKey(option)) {
      return type.cast(map.get(option));
    }
    return defaultValue;
  }
  
  private static void printHelp() {
    System.err.println("phpreboot [options] [file]\n" +
                       "  interpret the file or stdin\n\n" +
                       "  Options:\n");
    
    for(Option option: Option.values()) {
      System.err.println("   -"+option.name()+" "+option.help());
    }
  }
}
