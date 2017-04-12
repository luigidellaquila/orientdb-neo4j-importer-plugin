package com.orientechnologies.orient.http;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.neo4jimporter.ONeo4jImporter;
import com.orientechnologies.orient.neo4jimporter.ONeo4jImporterCommandLineParser;
import com.orientechnologies.orient.neo4jimporter.ONeo4jImporterPlugin;
import com.orientechnologies.orient.neo4jimporter.ONeo4jImporterSettings;
import com.orientechnologies.orient.outputmanager.OOutputStreamManager;
import com.orientechnologies.orient.server.OServer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabriele on 27/02/17.
 */
public class ONeo4jImporterJob  implements Runnable {

  private final ODocument cfg;
  private       ONeo4ImporterListener listener;
  public Status      status;

  public  PrintStream           stream;
  private ByteArrayOutputStream baos;

  private OServer currentServerInstance;

  public ONeo4jImporterJob(ODocument cfg, OServer currentServerInstance, ONeo4ImporterListener listener) {
    this.cfg = cfg;
    this.listener = listener;

    this.baos = new ByteArrayOutputStream();
    this.stream = new PrintStream(baos);

    this.currentServerInstance = currentServerInstance;
  }


  @Override
  public void run() {

    String neo4jUrl = cfg.field("neo4jUrl");
    String neo4jUsername = cfg.field("neo4jUsername");
    String neo4jPassword = cfg.field("neo4jPassword");
    String odbName = cfg.field("odbName");
    String odbProtocol = cfg.field("odbProtocol");
    boolean overrideDB = cfg.field("overwriteDB");
    boolean indexesOnRelationships = cfg.field("indexesOnRelationships");
    int logLevel = cfg.field("logLevel");

    status = Status.RUNNING;

    ONeo4jImporterSettings settings = new ONeo4jImporterSettings(neo4jUrl, neo4jUsername, neo4jPassword, odbName, odbProtocol, overrideDB, indexesOnRelationships);
    final ONeo4jImporterPlugin neo4jImporterPlugin = new ONeo4jImporterPlugin();

    try {
      String databaseDirectory = null;
      if (this.currentServerInstance != null) {
        databaseDirectory = this.currentServerInstance.getDatabaseDirectory();
      }
      neo4jImporterPlugin.executeJob(settings, new OOutputStreamManager(this.stream, logLevel), databaseDirectory);
    } catch (Exception e) {
      e.printStackTrace();
    }

    synchronized (listener) {
      status = Status.FINISHED;
      try {
        listener.wait(5000);
        listener.onEnd(this);
      } catch (InterruptedException e) {
      }
    }
  }

  public void validate() {

  }

  /**
   * Single Job Status
   *
   * @return ODocument
   */
  public ODocument status() {

    synchronized (listener) {
      ODocument status = new ODocument();
      status.field("cfg", cfg);
      status.field("status", this.status);
      status.field("log", baos.toString());
      if (this.status == Status.FINISHED) {
        listener.notifyAll();
      }
      return status;
    }

  }

  public enum Status {
    STARTED, RUNNING, FINISHED
  }
}