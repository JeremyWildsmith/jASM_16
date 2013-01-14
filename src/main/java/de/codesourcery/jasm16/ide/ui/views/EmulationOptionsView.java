package de.codesourcery.jasm16.ide.ui.views;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import de.codesourcery.jasm16.emulator.EmulationOptions;
import de.codesourcery.jasm16.emulator.EmulationOptions.InsertedDisk;

public abstract class EmulationOptionsView extends AbstractView {

	public static final String ID = "emulation_options";
	
	private JPanel panel;
	
	private final JCheckBox box1 = new JCheckBox("Write-protected memory of executed instructions (slow) ?");
	
	private final JCheckBox box2 = new JCheckBox("Enable debug output ?");
	private final JCheckBox box3 = new JCheckBox("Ignore access to unused device slots ?");
	private final JCheckBox box4 = new JCheckBox("Use legacy key buffer ?");
	private final JCheckBox box5 = new JCheckBox("Map video ram to 0x8000 on startup ?");
	private final JCheckBox box6 = new JCheckBox("Map font ram to 0x8180 on startup ?");
	private final JCheckBox box7 = new JCheckBox("Run floppy emulation at max speed ?");
	
	private final JPanel diskDrivePanel = new JPanel();
	private final JTextField selectedFileField = new JTextField();
	
	private final JButton fileChooserButton = new JButton("Choose image...");
	private final JCheckBox writeProtected = new JCheckBox("write-protected");
	
	public EmulationOptionsView(EmulationOptions options) 
	{
		if (options == null) {
			throw new IllegalArgumentException("options must not be null");
		}
		box1.setSelected( options.isMemoryProtectionEnabled() );
		box2.setSelected( options.isEnableDebugOutput() );
		box3.setSelected( options.isIgnoreAccessToUnknownDevices() );
		box4.setSelected( options.isUseLegacyKeyboardBuffer() );
		box5.setSelected( options.isMapVideoRamUponAddDevice() );
		box6.setSelected( options.isMapFontRamUponAddDevice() );
		box7.setSelected( options.isRunFloppyAtFullSpeed() );
		
		final InsertedDisk disk = options.getInsertedDisk();
		if ( disk == null ) 
		{
		    selectedFileField.setText( null );
		    writeProtected.setSelected( false );
		} else {
	          selectedFileField.setText( disk.getFile().getAbsolutePath() );
	          writeProtected.setSelected( disk.isWriteProtected() );
		}
		
		// disk drive panel
		selectedFileField.setColumns( 25 );
		diskDrivePanel.setLayout( new GridBagLayout() );
		GridBagConstraints cnstrs = constraints( 0, 0 , false , true , GridBagConstraints.NONE );
		cnstrs.anchor = GridBagConstraints.CENTER;
		diskDrivePanel.add( selectedFileField , cnstrs );
		
		cnstrs = constraints( 1, 0 , false , true , GridBagConstraints.NONE );
		cnstrs.anchor = GridBagConstraints.CENTER;
        diskDrivePanel.add( fileChooserButton , cnstrs );	
        
        fileChooserButton.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                final JFileChooser chooser;
                if ( getSelectedFile() != null ) 
                {
                    chooser = new JFileChooser( getSelectedFile().getParentFile() );
                } else {
                    chooser = new JFileChooser();
                }
                final int result = chooser.showOpenDialog(null);
                if ( result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().isFile() ) 
                {
                    selectedFileField.setText( chooser.getSelectedFile().getAbsolutePath() );
                }
            }
        } );

        cnstrs = constraints( 2, 0 , false , true , GridBagConstraints.NONE );
        cnstrs.anchor = GridBagConstraints.CENTER;
        diskDrivePanel.add( writeProtected , cnstrs );        
	}
	
	@Override
	public final void refreshDisplay()
	{
	}

	protected abstract void onSave(EmulationOptions options);
	
	protected abstract void onCancel();

	@Override
	public final String getTitle() {
		return "Emulation options";
	}

	private void saveChangesTo(EmulationOptions options) {
		options.setMemoryProtectionEnabled( box1.isSelected() );
		options.setEnableDebugOutput( box2.isSelected() );
		options.setIgnoreAccessToUnknownDevices( box3.isSelected() );
		options.setUseLegacyKeyboardBuffer( box4.isSelected() );
		options.setMapVideoRamUponAddDevice( box5.isSelected() );
		options.setMapFontRamUponAddDevice( box6.isSelected() );
		options.setRunFloppyAtFullSpeed( box7.isSelected() );
		
		if ( getSelectedFile() != null ) {
		    options.setInsertedDisk( new InsertedDisk(getSelectedFile(),writeProtected.isSelected() ) );
		} else {
		    options.setInsertedDisk( null );
		}
	}
	
	private File getSelectedFile() {
	    final String path = selectedFileField.getText();
	    return StringUtils.isBlank( path ) ? null : new File(path);
	}
	
	@Override
	public final String getID() {
		return ID;
	}

	@Override
	protected final JPanel getPanel() {
		if ( panel == null ) {
			panel = createPanel();
		}
		return panel;
	}
	
	private JPanel createPanel() {
		
		final JPanel result = new JPanel();
		
		result.setLayout( new GridBagLayout() );

		// 'Memory protection' checkbox
		int y = 0;
		GridBagConstraints cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
		cnstrs.gridwidth=2;
		result.add( box1 , cnstrs );
		
		cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
		cnstrs.gridwidth=2;
		result.add( box2 , cnstrs );
		
		cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
		cnstrs.gridwidth=2;
		result.add( box3 , cnstrs );
		
		cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
		cnstrs.gridwidth=2;
		result.add( box4 , cnstrs );
		
		cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
		cnstrs.gridwidth=2;
		result.add( box5 , cnstrs );
		
		cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
		cnstrs.gridwidth=2;
		result.add( box6 , cnstrs );
		
		cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
		cnstrs.gridwidth=2;
		result.add( box7 , cnstrs );		
		
        cnstrs = constraints( 0 , y++ , true , false , GridBagConstraints.HORIZONTAL );
        cnstrs.gridwidth=2;
        result.add( diskDrivePanel , cnstrs );    		
		
		// cancel button
		cnstrs = constraints( 0 , y , false , false , GridBagConstraints.NONE );
		cnstrs.weightx = 0.33;
		cnstrs.anchor = GridBagConstraints.CENTER;
		
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				onCancel();
			}
		} );
		result.add( cancelButton , cnstrs );
		
		// save button
		cnstrs = constraints( 1 , y , true , false , GridBagConstraints.NONE );
		cnstrs.weightx = 0.33;
		cnstrs.anchor = GridBagConstraints.CENTER;
		
		final JButton saveButton = new JButton("Save");
		saveButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				final EmulationOptions options = new EmulationOptions();
				saveChangesTo( options );
				onSave( options );
			}
			
		} );
		result.add( saveButton , cnstrs );		

		return result;
	}
}