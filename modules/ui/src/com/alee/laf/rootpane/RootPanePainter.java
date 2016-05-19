package com.alee.laf.rootpane;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.style.StyleId;
import com.alee.painter.decoration.AbstractContainerPainter;
import com.alee.painter.decoration.DecorationState;
import com.alee.painter.decoration.IDecoration;
import com.alee.utils.CompareUtils;
import com.alee.utils.ProprietaryUtils;
import com.alee.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.util.List;

/**
 * Basic painter for {@link javax.swing.JRootPane} component.
 * It is used as {@link com.alee.laf.rootpane.WebRootPaneUI} default painter.
 *
 * @param <E> component type
 * @param <U> component UI type
 * @param <D> decoration type
 * @author Alexandr Zernov
 * @author Mikle Garin
 */

public class RootPanePainter<E extends JRootPane, U extends WebRootPaneUI, D extends IDecoration<E, D>>
        extends AbstractContainerPainter<E, U, D> implements IRootPanePainter<E, U>
{
    /**
     * Listeners.
     */
    protected WindowFocusListener windowFocusListener;
    protected WindowStateListener frameStateListener;

    /**
     * Runtime variables.
     */
    protected boolean maximized = false;

    @Override
    public void install ( final E c, final U ui )
    {
        super.install ( c, ui );

        // Installing window-related settings
        final Window window = SwingUtils.getWindowAncestor ( c );
        if ( window != null )
        {
            // Enabling window decorations
            if ( isDecorated () )
            {
                enableWindowDecoration ( c, window );
            }

            // Adding window focus listener
            if ( usesState ( DecorationState.focused ) )
            {
                windowFocusListener = new WindowFocusListener ()
                {
                    @Override
                    public void windowGainedFocus ( final WindowEvent e )
                    {
                        // Updating focus state
                        RootPanePainter.this.focused = true;

                        // Updating decoration
                        if ( isDecorated () )
                        {
                            updateDecorationState ();
                        }
                    }

                    @Override
                    public void windowLostFocus ( final WindowEvent e )
                    {
                        // Updating decoration
                        RootPanePainter.this.focused = false;

                        // Updating decoration
                        if ( isDecorated () )
                        {
                            updateDecorationState ();
                        }
                    }
                };
                window.addWindowFocusListener ( windowFocusListener );
            }

            // Adding frame state change listener
            if ( window instanceof Frame )
            {
                frameStateListener = new WindowStateListener ()
                {
                    @Override
                    public void windowStateChanged ( final WindowEvent e )
                    {
                        if ( isDecorated () )
                        {
                            updateDecorationState ();
                        }
                    }
                };
                window.addWindowStateListener ( frameStateListener );
            }
        }
    }

    @Override
    public void uninstall ( final E c, final U ui )
    {
        // Uninstalling window-related settings
        final Window window = SwingUtils.getWindowAncestor ( c );
        if ( window != null )
        {
            // Removing frame state change listener
            if ( frameStateListener != null )
            {
                window.removeWindowStateListener ( frameStateListener );
                frameStateListener = null;
            }

            // Removing window focus listener
            if ( windowFocusListener != null )
            {
                window.removeWindowFocusListener ( windowFocusListener );
                windowFocusListener = null;
            }

            // Disabling window decorations
            if ( isDecorated () )
            {
                disableWindowDecoration ( c, window );
            }
        }

        super.uninstall ( c, ui );
    }

    @Override
    protected void installFocusListener ()
    {
        // Disable default focus listener installation
        // Custom focus listener is used for this painter
    }

    @Override
    protected void uninstallFocusListener ()
    {
        // Disable default focus listener deinstallation
        // Custom focus listener is used for this painter
    }

    @Override
    protected void propertyChange ( final String property, final Object oldValue, final Object newValue )
    {
        // Perform basic actions on property changes
        super.propertyChange ( property, oldValue, newValue );

        // Updating decoration according to current state
        if ( CompareUtils.equals ( property, WebLookAndFeel.WINDOW_DECORATION_STYLE_PROPERTY ) )
        {
            // Updating decoration state first
            // This is necessary to avoid issues with state-dependant decorations
            updateDecorationState ();

            // Removing window decoration
            // This is required to disable custom window decoration upon this property change
            // We automatically rollback style here if the property was set from a custom one to JRootPane.NONE
            if ( !CompareUtils.equals ( oldValue, JRootPane.NONE ) && CompareUtils.equals ( newValue, JRootPane.NONE ) &&
                    !CompareUtils.equals ( StyleId.get ( component ), StyleId.rootpane, StyleId.dialog, StyleId.frame ) )
            {
                // Restoring original undecorated style
                final Window window = SwingUtils.getWindowAncestor ( component );
                if ( window != null )
                {
                    if ( window instanceof Frame )
                    {
                        StyleId.frame.set ( component );
                    }
                    else if ( window instanceof Dialog )
                    {
                        StyleId.dialog.set ( component );
                    }
                    else
                    {
                        StyleId.rootpane.set ( component );
                    }
                }
                else
                {
                    StyleId.rootpane.set ( component );
                }
            }

            // Installing default window decoration
            // This is an important workaround to allow Frame and Dialog decoration to be enabled according to settings
            // We only apply that workaround to non WebFrame/WebDialog based frames and dialogs which have decoration style specified
            if ( CompareUtils.equals ( oldValue, JRootPane.NONE ) && !CompareUtils.equals ( newValue, JRootPane.NONE ) &&
                    CompareUtils.equals ( StyleId.get ( component ), StyleId.rootpane, StyleId.dialog, StyleId.frame ) )
            {
                final Window window = SwingUtils.getWindowAncestor ( component );
                if ( window != null )
                {
                    if ( window instanceof Frame )
                    {
                        StyleId.frameDecorated.set ( component );
                    }
                    else if ( window instanceof Dialog )
                    {
                        switch ( component.getWindowDecorationStyle () )
                        {
                            case JRootPane.COLOR_CHOOSER_DIALOG:
                            {
                                StyleId.colorchooserDialog.set ( component );
                                break;
                            }
                            case JRootPane.FILE_CHOOSER_DIALOG:
                            {
                                StyleId.filechooserDialog.set ( component );
                                break;
                            }
                            case JRootPane.INFORMATION_DIALOG:
                            {
                                StyleId.optionpaneInformationDialog.set ( component );
                                break;
                            }
                            case JRootPane.ERROR_DIALOG:
                            {
                                StyleId.optionpaneErrorDialog.set ( component );
                                break;
                            }
                            case JRootPane.QUESTION_DIALOG:
                            {
                                StyleId.optionpaneQuestionDialog.set ( component );
                                break;
                            }
                            case JRootPane.WARNING_DIALOG:
                            {
                                StyleId.optionpaneWarningDialog.set ( component );
                                break;
                            }
                            default:
                            {
                                StyleId.dialogDecorated.set ( component );
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected List<String> getDecorationStates ()
    {
        final List<String> states = super.getDecorationStates ();
        states.add ( getWindowDecorationState () );
        if ( ui.isIconified () )
        {
            states.add ( DecorationState.iconified );
        }
        if ( ui.isMaximized () )
        {
            states.add ( DecorationState.maximized );
        }
        if ( SwingUtils.isFullScreen ( component ) )
        {
            states.add ( DecorationState.fullscreen );
        }
        return states;
    }

    /**
     * Returns window decoration state.
     *
     * @return window decoration state
     */
    protected String getWindowDecorationState ()
    {
        switch ( component.getWindowDecorationStyle () )
        {
            case JRootPane.NONE:
            {
                return DecorationState.nativeWindow;
            }
            case JRootPane.FRAME:
            {
                return DecorationState.frame;
            }
            case JRootPane.PLAIN_DIALOG:
            {
                return DecorationState.dialog;
            }
            case JRootPane.COLOR_CHOOSER_DIALOG:
            {
                return DecorationState.colorchooserDialog;
            }
            case JRootPane.FILE_CHOOSER_DIALOG:
            {
                return DecorationState.filechooserDialog;
            }
            case JRootPane.INFORMATION_DIALOG:
            {
                return DecorationState.informationDialog;
            }
            case JRootPane.ERROR_DIALOG:
            {
                return DecorationState.errorDialog;
            }
            case JRootPane.QUESTION_DIALOG:
            {
                return DecorationState.questionDialog;
            }
            case JRootPane.WARNING_DIALOG:
            {
                return DecorationState.warningDialog;
            }
        }
        throw new RuntimeException ( "Unknown window decoration style: " + component.getWindowDecorationStyle () );
    }

    /**
     * Enables root pane window decoration.
     *
     * @param c      root pane
     * @param window window to enable decoration for
     */
    protected void enableWindowDecoration ( final E c, final Window window )
    {
        // Enabling frame decoration
        if ( window instanceof Frame )
        {
            if ( !window.isDisplayable () )
            {
                component.setOpaque ( false );
                ProprietaryUtils.setWindowShape ( window, null );
                ( ( Frame ) window ).setUndecorated ( true );
                ProprietaryUtils.setWindowOpaque ( window, false );
            }
            if ( c.getWindowDecorationStyle () == JRootPane.NONE )
            {
                c.setWindowDecorationStyle ( JRootPane.FRAME );
            }
        }
        else if ( window instanceof Dialog )
        {
            if ( !window.isDisplayable () )
            {
                component.setOpaque ( false );
                ProprietaryUtils.setWindowShape ( window, null );
                ( ( Dialog ) window ).setUndecorated ( true );
                ProprietaryUtils.setWindowOpaque ( window, false );
            }
            if ( c.getWindowDecorationStyle () == JRootPane.NONE )
            {
                c.setWindowDecorationStyle ( JRootPane.PLAIN_DIALOG );
            }
        }

        // Installing UI decorations
        ui.installWindowDecorations ();
    }

    /**
     * Disables root pane window decoration.
     *
     * @param c      root pane
     * @param window window to disable decoration for
     */
    protected void disableWindowDecoration ( final E c, final Window window )
    {
        // Uninstalling UI decorations
        ui.uninstallWindowDecorations ();

        // Disabling frame decoration
        if ( window instanceof Frame )
        {
            if ( !window.isDisplayable () )
            {
                ProprietaryUtils.setWindowOpaque ( window, true );
                ( ( Frame ) window ).setUndecorated ( false );
                component.setOpaque ( true );
            }
            if ( c.getWindowDecorationStyle () != JRootPane.NONE )
            {
                c.setWindowDecorationStyle ( JRootPane.NONE );
            }
        }
        else if ( window instanceof Dialog )
        {
            if ( !window.isDisplayable () )
            {
                ProprietaryUtils.setWindowOpaque ( window, true );
                ( ( Dialog ) window ).setUndecorated ( false );
                component.setOpaque ( true );
            }
            if ( c.getWindowDecorationStyle () != JRootPane.NONE )
            {
                c.setWindowDecorationStyle ( JRootPane.NONE );
            }
        }
    }

    /**
     * Returns whether or not root pane window is currently active.
     *
     * @param c root pane
     * @return true if root pane window is currently active, false otherwise
     */
    protected boolean isActive ( final E c )
    {
        return SwingUtils.getWindowAncestor ( c ).isFocused ();
    }
}