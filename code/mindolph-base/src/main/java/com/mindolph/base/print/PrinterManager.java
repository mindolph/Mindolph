package com.mindolph.base.print;

import javafx.collections.ObservableSet;
import javafx.print.Printer;
import javafx.print.PrinterJob;

public class PrinterManager {

    private static final PrinterManager ins = new PrinterManager();

    public static PrinterManager getInstance() {
        return ins;
    }

    private PrinterManager() {
    }

    public Printer getFirstPrinter() {
        ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
        if (allPrinters.isEmpty()) {
            return null;
        }
        return allPrinters.stream().findFirst().get();
    }

    public PrinterJob createPrinterJob() {
        Printer firstPrinter = getFirstPrinter();
        return PrinterJob.createPrinterJob(firstPrinter);
    }
}
