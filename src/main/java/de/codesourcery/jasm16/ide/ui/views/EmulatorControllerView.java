package de.codesourcery.jasm16.ide.ui.views;

/**
 * Copyright 2012 Tobias Gierke <tobias.gierke@code-sourcery.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.codesourcery.jasm16.emulator.EmulationListener;
import de.codesourcery.jasm16.emulator.IEmulationListener;
import de.codesourcery.jasm16.emulator.IEmulator;
import de.codesourcery.jasm16.ide.ui.utils.UIUtils;
import de.codesourcery.jasm16.ide.ui.viewcontainers.DebuggingPerspective;

public class EmulatorControllerView extends AbstractView
{
    public static final String VIEW_ID = "emulator-controller-view";
    
    private JPanel panel;
    private final JButton singleStepButton = new JButton("Step");
    private final JButton runButton = new JButton("Run");
    private final JButton stopButton = new JButton("Stop");   
    private final JButton resetButton = new JButton("Reset");
    private JCheckBox runAtRealSpeed;
    
    private final DebuggingPerspective perspective;
    private IEmulator emulator;
    
    private final IEmulationListener listener = new EmulationListener() {

        @Override
        public void afterReset(IEmulator emulator)
        {
            updateButtonStates( false );            
        }
        
        @Override
        protected void beforeContinuousExecutionHook() {
            updateButtonStates( true );         
        };
        
        @Override
        public void afterContinuousExecutionHook() {
            updateButtonStates( false );
        }        
     };
     
    public EmulatorControllerView(DebuggingPerspective perspective, IEmulator emulator) {
        if ( perspective == null ) {
            throw new IllegalArgumentException("perspective must not be null");
        }
        this.perspective = perspective;
        setEmulator( emulator );
    }
    
    private void updateButtonStates(final boolean emulatorRunningContinously) {

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                singleStepButton.setEnabled( ! emulatorRunningContinously );
                runButton.setEnabled( ! emulatorRunningContinously );
                stopButton.setEnabled( emulatorRunningContinously );
                resetButton.setEnabled( true );  
            }
        };

        if ( SwingUtilities.isEventDispatchThread() ) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater( runnable );
        }
    }
    
    @Override
    public void refreshDisplay() 
    {
    }
    
    public void setEmulator(IEmulator emulator)
    {
        if (emulator == null) {
            throw new IllegalArgumentException("emulator must not be NULL.");
        }
        if ( this.emulator == emulator ) {
            return;
        }
        
        if ( this.emulator != null ) {
            this.emulator.removeEmulationListener( listener );
        }
        this.emulator = emulator;
        
        emulator.addEmulationListener( listener );
    }
    
    @Override
    public void dispose() 
    {
        if ( this.emulator != null ) {
            this.emulator.removeEmulationListener( listener );
            this.emulator = null;
        }
    }
    
    protected JPanel createPanel()
    {
        // setup top panel
        final JPanel buttonBar = new JPanel();
        buttonBar.setLayout( new GridBagLayout() );        

        // =========== "SINGLE STEP" button ============        
        singleStepButton.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                emulator.executeOneInstruction();
            }
        });
        
        GridBagConstraints cnstrs = constraints( 0 , 0 , false , true , GridBagConstraints.NONE );          
        buttonBar.add( singleStepButton , cnstrs );
        
        // =========== "RUN" button ============        
        runButton.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                emulator.start();
                System.out.println("Simulation started!");
            }
        });
        
        cnstrs = constraints( 1 , 0 , false , true , GridBagConstraints.NONE );          
        buttonBar.add( runButton , cnstrs );   
        
        // =========== "STOP" button ============        
        stopButton.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                emulator.stop();
            }
        });
        
        cnstrs = constraints( 2 , 0 , false , true , GridBagConstraints.NONE );          
        buttonBar.add( stopButton , cnstrs );          
        
        // =========== "RESET" button ============
        resetButton.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                perspective.resetEmulator();
            }
        });
        
        cnstrs = constraints( 3 , 0 , false , true , GridBagConstraints.NONE );          
        buttonBar.add( resetButton , cnstrs );
        
        // =========== "Run at full speed" checkbox ============
        runAtRealSpeed = new JCheckBox("Run at real speed",emulator.isRunAtRealSpeed());
        final AtomicBoolean isCalibrating = new AtomicBoolean(false);
        runAtRealSpeed.addActionListener( new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final boolean isSelected = runAtRealSpeed.isSelected();
                if ( isSelected ) 
                {
                    if ( isCalibrating.compareAndSet( false , true ) ) 
                    {
                        new Thread() {
                            @Override
                            public void run() 
                            {
                                final JDialog dialog = UIUtils.createMessageDialog( null, "Calibrating emulation speed" , "Please wait, benchmarking your system...");
                                dialog.setModal(true);
                                
                                new Thread() {
                                    @Override
                                    public void run() 
                                    {
                                        try {
                                            emulator.calibrate();
                                            emulator.setRunAtRealSpeed( true );
                                        } 
                                        finally {
                                            isCalibrating.set( false );                                         
                                            dialog.dispose();
                                        }
                                    }
                                }.start();
                                dialog.setVisible( true );
                            }
                        }.start();
                    }
                } else {
                    emulator.setRunAtRealSpeed( isSelected );
                }
            }
        });
        
        cnstrs = constraints( 4 , 0 , true , true , GridBagConstraints.NONE );          
        buttonBar.add( runAtRealSpeed , cnstrs );        
        
        updateButtonStates( false );
        return buttonBar;
    }

    @Override
    public JPanel getPanel() {
        if ( panel == null ) {
            panel = createPanel();
        }
        return panel;
    }

    @Override
    public String getTitle() {
        return "Emulator control";
    }

    @Override
    public String getID() {
        return VIEW_ID;
    }
    
}