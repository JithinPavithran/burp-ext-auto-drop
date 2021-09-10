package burp;

import proxy.Drop;
import proxy.IProxyComponent;
import proxy.ProxyMessageContainer;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class BurpExtender implements IBurpExtender, ITab, IProxyListener {

    private final String name = "Advanced Proxy";
    private final String tabName = "AdvProxy";

    private List<IProxyComponent> IProxyComponents = new ArrayList<>();

    private JPanel tabUi;
    public PrintWriter stdout;
    public PrintWriter stderr;
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers; // in case required in the future :)

    public BurpExtender() { }

    private void initComponents() {
        this.IProxyComponents.add(new Drop(this));
    }

    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.stdout = new PrintWriter(callbacks.getStdout(), true);
        this.stderr = new PrintWriter(callbacks.getStderr(), true);
        callbacks.setExtensionName(this.name);
        initComponents();
        this.createTabUi();
        callbacks.addSuiteTab(this);
        this.helpers = callbacks.getHelpers();
        callbacks.registerProxyListener(this);
        this.stdout.println("Loaded" + this.name + "Extension");
    }

    @Override
    public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
        ProxyMessageContainer pmc = new ProxyMessageContainer(message);
        if (messageIsRequest) {
            for(int i = 0; i < this.IProxyComponents.size() && this.IProxyComponents.get(i).processRequest(pmc); i++) ;
        } else {
            for(int i = 0; i < this.IProxyComponents.size() && this.IProxyComponents.get(i).processResponse(pmc); i++) ;
        }
    }

    @Override
    public String getTabCaption() {
        return this.tabName;
    }

    /**
     * Burp uses this method to obtain the component that should be used as the
     * contents of the custom tab when it is displayed.
     *
     * @return The component that should be used as the contents of the custom
     * tab when it is displayed.
     */
    @Override
    public Component getUiComponent() {
        return tabUi;
    }

    private void createTabUi() {
        this.tabUi = new JPanel();
        createLoaders();
        callbacks.customizeUiComponent(tabUi);
    }

    private void createLoaders() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for(IProxyComponent pc: this.IProxyComponents) {
            JPanel p = pc.getPanel();
            if (p != null) {
//                JLabel title = new JLabel(pc.getName());
//                Font font = title.getFont();
//                title.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
//                panel.add(title);
                panel.add(p);
                panel.add(new JSeparator());
            }
        }
        this.tabUi.setLayout(new BorderLayout());
        this.tabUi.add(panel, BorderLayout.PAGE_START);
        System.gc();
    }

}
