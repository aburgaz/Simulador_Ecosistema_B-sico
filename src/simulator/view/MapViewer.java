package simulator.view;

import simulator.model.Animal;
import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.State;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

    // Anchura/altura/ de la simulación -- se supone que siempre van a ser iguales
    // al tamaño del componente
    private int _width;
    private int _height;

    // Number of rows and columns in the map
    private int _rows;
    private int _cols;

    // Width and height of each region
    int _rwidth;
    int _rheight;

    // Actual state
    State _currState;    // Possible values: null or State.values()

    int i = 0;    // To iterate over State.values()

    // Help text
    final String helpText1 = "h: toggle help";
    final String helpText2 = "s: show animals of a specific state";

    // List of animals and time, needed to draw them on the map.
    volatile private Collection<AnimalInfo> _objs;
    volatile private Double _time;

    // Auxiliar class to store information about the species
    private static class SpeciesInfo {
        private Integer _count;        // Number of animals of this species
        private Color _color;     // Color to use to draw the animals of this species

        SpeciesInfo(Color color) {
            _count = 0;
            _color = color;
        }
    }

    // Map to store the information about the species
    Map<String, SpeciesInfo> _kindsInfo = new HashMap<>();

    // Font to draw the text
    private Font _font = new Font("Arial", Font.BOLD, 12);

    // Show help text or not
    private boolean _showHelp;

    public MapViewer() {
        initGUI();
    }

    private void initGUI() {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'h':
                        _showHelp = !_showHelp;
                        repaint();    // Repaint the component to show the help text or not
                        break;
                    case 's':
                        if (i == State.values().length) {    // If we have iterated over all the states
                            _currState = null;
                        } else {
                            _currState = State.values()[i];
                        }
                        i++;
                        i %= State.values().length + 1;    // To iterate over State.values() and null
                        repaint();
                    default:
                }
            }

        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                requestFocus(); // Necessary to capture key events while the mouse is inside the component
            }
        });

        // Default values
        _currState = null;
        _showHelp = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D gr = (Graphics2D) g;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Change font to draw the text
        g.setFont(_font);

        // Set white background
        gr.setBackground(Color.WHITE);
        gr.clearRect(0, 0, _width, _height);

        // Draw grid
        for (int x = 0; x <= _cols; x++)
            for (int y = 0; y <= _rows; y++)
                g.drawRect(x * _rwidth, y * _rheight, _rwidth, _rheight);

        // Draw the animals, time and info
        if (_objs != null)
            drawObjects(gr, _objs, _time);

        // Draw help text if selected
        if (_showHelp) {
            g.setColor(Color.RED);
            g.drawString(helpText1, 20, 30);
            g.drawString(helpText2, 20, 50);
        }
    }

    private boolean visible(AnimalInfo a) {
        return (_currState == null) || (a.get_state() == _currState);
    }

    private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {

        _kindsInfo.clear();        // Clear the information about the species

        // Draw the animals
        for (AnimalInfo a : animals) {
            if (!visible(a))    // Only if visible
                continue;

            SpeciesInfo esp_info = _kindsInfo.get(a.get_genetic_code());    // Get animal species information

            if (esp_info == null) {    // If the species is not in the map fill it
                SpeciesInfo specimen = new SpeciesInfo(ViewUtils.get_color(a.get_genetic_code()));
                _kindsInfo.put(a.get_genetic_code(), specimen);
            }

            _kindsInfo.get(a.get_genetic_code())._count++;     // Increase the number of animals of this species

            g.setColor(_kindsInfo.get(a.get_genetic_code())._color);    // Set the color of the animal
            g.fillRoundRect((int) a.get_position().getX(), (int) a.get_position().getY(), (int) a.get_age() / 2 + 2, (int) a.get_age() / 2 + 2, 1, 1);    // Draw the animal
        }

        Rectangle2D rect = g.getFontMetrics().getStringBounds(" ", g);    // Rectangle to extract the size of the text
        int j = 2;    // Iterator to draw the text in the correct position
        g.setColor(Color.MAGENTA);
        drawStringWithRect(g, 20, _height - (int) (rect.getHeight() * j) - 5 * j, String.format("%.3f", time));    // Draw the time
        j++;    // After each text we increase the iterator to draw the next text in the correct position
        for (Entry<String, SpeciesInfo> e : _kindsInfo.entrySet()) {    // Draw the information about the species
            g.setColor(e.getValue()._color);    // Set the color of the text
            drawStringWithRect(g, 20, _height - (int) (rect.getHeight() * j) - 5 * j, e.getKey() + ": " + e.getValue()._count.toString());    // Draw the text
            j++;
        }
        g.setColor(Color.BLUE);
        if (_currState != null)
            drawStringWithRect(g, 20, _height - (int) (rect.getHeight() * j) - 5 * j, _currState.toString());     // Draw the state
    }

    // To draw a string within a rectangle
    void drawStringWithRect(Graphics2D g, int x, int y, String s) {
        Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
        g.drawString(s, x, y);
        g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
    }

    @Override
    public void update(List<AnimalInfo> objs, Double time) {
        _objs = objs;
        _time = time;
        repaint();
    }

    @Override
    public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
        _width = map.get_width();
        _height = map.get_height();
        _cols = map.get_cols();
        _rows = map.get_rows();
        _rheight = map.get_region_height();
        _rwidth = map.get_region_width();

        // This changes the size of the component, also the size of the map window (pack())
        setPreferredSize(new Dimension(map.get_width(), map.get_height()));

        // Update and paint the component
        update(animals, time);
    }

}
