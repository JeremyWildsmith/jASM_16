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
package de.codesourcery.jasm16.ide;

import de.codesourcery.jasm16.emulator.Emulator;
import de.codesourcery.jasm16.emulator.ILogger;
import de.codesourcery.jasm16.emulator.PrintStreamLogger;
import de.codesourcery.jasm16.emulator.devices.impl.DefaultClock;
import de.codesourcery.jasm16.emulator.devices.impl.DefaultKeyboard;
import de.codesourcery.jasm16.emulator.devices.impl.DefaultScreen;

public class EmulatorFactory
{
    public Emulator createEmulator() {
        final Emulator result = new Emulator();
        ILogger outLogger = new PrintStreamLogger( System.out );
        outLogger.setDebugEnabled( false );
		result.setOutput( outLogger );
        result.setMemoryProtectionEnabled( false );
        result.setIgnoreAccessToUnknownDevices( false );
        result.addDevice( new DefaultClock() );
        return result;
    }
    
    public DefaultKeyboard createKeyboardDevice() {
    	return new DefaultKeyboard( true );
    }
    
    public DefaultScreen createScreenDevice() {
    	return new DefaultScreen( true , false );
    }
}
