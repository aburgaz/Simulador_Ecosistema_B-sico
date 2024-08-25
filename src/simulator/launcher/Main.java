package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.*;
import simulator.misc.Utils;
import simulator.model.*;
import simulator.view.MainWindow;

public class Main {
    private enum ExecMode {
        BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

        private String _tag;
        private String _desc;

        private ExecMode(String modeTag, String modeDesc) {
            _tag = modeTag;
            _desc = modeDesc;
        }

        public String get_tag() {
            return _tag;
        }

        public String get_desc() {
            return _desc;
        }
    }

    // default values for some parameters
    //
    private final static Double _default_time = 10.0; // in seconds
    private final static Double _default_dt = 0.03; // in seconds

    // some attributes to stores values corresponding to command-line parameters
    //
    private static Double _time = _default_time;
    public static Double _dtime = _default_dt;
    private static boolean _sv = false;
    private static String _in_file = null;
    private static String _out_file = null;
    private static Simulator _sim;
    private static Controller _controller;
    private static ExecMode _mode = ExecMode.GUI;
    private static final int _default_width = 800;
    private static final int _default_height = 600;
    private static final int _default_rows = 20;
    private static final int _default_cols = 15;

    public static Factory<SelectionStrategy> _selection_strategy_factory;
    public static Factory<Animal> _animals_factory;
    public static Factory<Region> _region_factory;

    private static void parse_args(String[] args) {

        // define the valid command line options
        //
        Options cmdLineOptions = build_options();

        // parse the command line as provided in args
        //
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(cmdLineOptions, args);
            parse_help_option(line, cmdLineOptions);
            parse_mode_option(line);
            parse_in_file_option(line);
            parse_time_option(line);
            parse_dtime_option(line);
            parse_out_file_option(line);
            parse_sv_option(line);

            // if there are some remaining arguments, then something wrong is
            // provided in the command line!
            //
            String[] remaining = line.getArgs();
            if (remaining.length > 0) {
                String error = "Illegal arguments:";
                for (String o : remaining)
                    error += (" " + o);
                throw new ParseException(error);
            }

        } catch (ParseException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }

    }

    private static Options build_options() {
        Options cmdLineOptions = new Options();

        // help
        cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

        // input file
        cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("Initial configuration file.").build());

        // output file
        cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());
        
        // mode
        cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg().desc("Execution Mode. Possible values: 'batch' (Batch mode), 'gui' (Graphical User Interface mode). Default value: 'gui'.").build());
        
        // steps
        cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg().desc("A real number representing the total simulation time in seconds. Default value: " + _default_time + ".").build());

        // delta time
        cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg().desc("A double representing actual time, in seconds, per simulation step. Default value: " + _default_dt + ".").build());

        // simple viewer
        cmdLineOptions.addOption(Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

        return cmdLineOptions;
    }

    private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
        if (line.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
            System.exit(0);
        }
    }

    private static void parse_mode_option(CommandLine line) throws ParseException {
        if(line.hasOption("m")) {
            String m = line.getOptionValue("m", _mode.get_tag());
            if (m.equalsIgnoreCase(ExecMode.BATCH.get_tag())) {
                _mode = ExecMode.BATCH;
            } else if (m.equalsIgnoreCase(ExecMode.GUI.get_tag())) {
                _mode = ExecMode.GUI;
            } else {
                throw new ParseException("Invalid value for mode: " + m);
            }
        }
    }

    private static void parse_in_file_option(CommandLine line) throws ParseException {
        _in_file = line.getOptionValue("i");
        if (_mode == ExecMode.BATCH && _in_file == null) {
            throw new ParseException("In batch mode an input configuration file is required");
        }
    }

    private static void parse_time_option(CommandLine line) throws ParseException {
        if(_mode == ExecMode.BATCH) {
            String t = line.getOptionValue("t", _default_time.toString());
            try {
                _time = Double.parseDouble(t);
                assert (_time >= 0);
            } catch (Exception e) {
                throw new ParseException("Invalid value for time: " + t);
            }
        }
    }

    private static void parse_dtime_option(CommandLine line) throws ParseException {
        String dt = line.getOptionValue("dt", _default_dt.toString());
        try {
            _dtime = Double.parseDouble(dt);
            assert (_dtime >= 0);
        } catch (Exception e) {
            throw new ParseException("Invalid value for time: " + dt);
        }
    }

    private static void parse_out_file_option(CommandLine line) throws ParseException {
        if(_mode == ExecMode.BATCH) {
            _out_file = line.getOptionValue("o");
            if (_out_file == null) {
                throw new ParseException("In batch mode an output file is required");
            }
        }   
    }

    private static void parse_sv_option(CommandLine line) throws ParseException {
        if (line.hasOption("sv")) {
            _sv = true;
        }
    }

    private static void init_factories() {
        // initialize the strategies factory
        List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
        selection_strategy_builders.add(new SelectFirstBuilder());
        selection_strategy_builders.add(new SelectClosestBuilder());
        selection_strategy_builders.add(new SelectYoungestBuilder());

        _selection_strategy_factory = new BuilderBasedFactory<SelectionStrategy>(selection_strategy_builders);

        // initialize the animals factory
        List<Builder<Animal>> animal_builders = new ArrayList<>();
        animal_builders.add(new SheepBuilder(_selection_strategy_factory));
        animal_builders.add(new WolfBuilder(_selection_strategy_factory));

        _animals_factory = new BuilderBasedFactory<Animal>(animal_builders);

        // initialize the region factory
        List<Builder<Region>> region_builders = new ArrayList<>();
        region_builders.add(new DefaultRegionBuilder());
        region_builders.add(new DynamicSupplyRegionBuilder());

        _region_factory = new BuilderBasedFactory<Region>(region_builders);
    }

    private static JSONObject load_JSON_file(InputStream in) {
        return new JSONObject(new JSONTokener(in));
    }

    private static void start_batch_mode() throws Exception {
        InputStream is = new FileInputStream(new File(_in_file));
        OutputStream os = new FileOutputStream(new File(_out_file));

        JSONObject jo = load_JSON_file(is);

        int width = jo.getInt("width");
        int height = jo.getInt("height");
        int rows = jo.getInt("rows");
        int cols = jo.getInt("cols");

        _sim = new Simulator(cols, rows, width, height, _animals_factory, _region_factory);
        _controller = new Controller(_sim);
        _controller.load_data(jo);
        _controller.run(_time, _dtime, _sv, os);

        is.close();
        os.close();
    }

    private static void start_GUI_mode() throws Exception {
        if(_in_file == null) {      // if no input file is provided, start with default values
            _sim = new Simulator(_default_cols, _default_rows, _default_width, _default_height, _animals_factory, _region_factory);
            _controller = new Controller(_sim);
            SwingUtilities.invokeAndWait(() -> new MainWindow(_controller));
        }
        else{       // same as batch mode, but with GUI invoked instead of run() method
            InputStream is = new FileInputStream(new File(_in_file));

            JSONObject jo = load_JSON_file(is);
    
            int width = jo.getInt("width");
            int height = jo.getInt("height");
            int rows = jo.getInt("rows");
            int cols = jo.getInt("cols");
    
            _sim = new Simulator(cols, rows, width, height, _animals_factory, _region_factory);
            _controller = new Controller(_sim);
            SwingUtilities.invokeAndWait(() -> new MainWindow(_controller));        // fist create the GUI to add the observers
            _controller.load_data(jo);      // then load the data and notify the observers
        }
    }

    private static void start(String[] args) throws Exception {
        init_factories();
        parse_args(args);
        switch (_mode) {
            case BATCH:
                start_batch_mode();
                break;

            case GUI:
                start_GUI_mode();
                break;
        }
    }

    public static void main(String[] args) {
        Utils._rand.setSeed(2147483647l);
        try {
            start(args);
        } catch (Exception e) {
            System.err.println("Something went wrong ...");
            System.err.println();
            e.printStackTrace();
        }
    }
}
