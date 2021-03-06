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
package de.codesourcery.jasm16.emulator.devices;

import de.codesourcery.jasm16.utils.Misc;

/**
 * An interrupt triggered by a hardware device.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public final class HardwareInterrupt implements IInterrupt {

	private final IDcpuHardware device;
	private final int message;
	
	public HardwareInterrupt(IDcpuHardware device,int message) {
		if (device == null) {
			throw new IllegalArgumentException("device must not be null");
		}
		this.device = device;
		this.message = message & 0xffff;
	}
	
	public IDcpuHardware getDevice() {
		return device;
	}
	
	@Override
	public int getMessage() {
		return message;
	}

	@Override
	public boolean isSoftwareInterrupt() {
		return false;
	}

	@Override
	public boolean isHardwareInterrupt() {
		return true;
	}

	@Override
	public String toString() {
		return "HW{"+Misc.toHexString( message )+"} , device "+device.getDeviceDescriptor().getName() ;
	}
}
