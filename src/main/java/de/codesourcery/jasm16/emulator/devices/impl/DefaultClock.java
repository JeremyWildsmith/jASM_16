/**
 * Copyright 2012 Tobias Gierke <tobias.gierke@code-sourcery.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.codesourcery.jasm16.emulator.devices.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import de.codesourcery.jasm16.Address;
import de.codesourcery.jasm16.Register;
import de.codesourcery.jasm16.emulator.EmulationListener;
import de.codesourcery.jasm16.emulator.ICPU;
import de.codesourcery.jasm16.emulator.IEmulationListener;
import de.codesourcery.jasm16.emulator.IEmulator;
import de.codesourcery.jasm16.emulator.ILogger;
import de.codesourcery.jasm16.emulator.devices.DeviceDescriptor;
import de.codesourcery.jasm16.emulator.devices.HardwareInterrupt;
import de.codesourcery.jasm16.emulator.memory.IMemory;
import de.codesourcery.jasm16.utils.Misc;
import de.codesourcery.jasm16.emulator.devices.IDcpuHardware;

public class DefaultClock implements IDcpuHardware {

	private static final int INITIAL_TICKS_PER_SECOND = (int) Math.round(1000.0 / 60.0d);

	private static final DeviceDescriptor DESC = new DeviceDescriptor("Generic clock",
			"Generic clock (compatible)", 0x12d0b402, 1, Constants.JASM16_MANUFACTURER);

	private volatile IEmulator emulator;

	private final ClockHandler clockHandler = new ClockHandler();

	private volatile ILogger out;

	private final AtomicBoolean emulationRunning = new AtomicBoolean(false);
	
	private final IEmulationListener myEmulationListener = new EmulationListener() {

		public boolean belongsToHardwareDevice() {
			return true;
		}

		protected void beforeContinuousExecutionHook() {
			emulationRunning.set(true);
		}

		public void onStopHook(IEmulator emulator, Address previousPC, Throwable emulationError) {
			emulationRunning.set(false);
		}
	};

	@Override
	public void reset() {
		clockHandler.sleepDelay = INITIAL_TICKS_PER_SECOND;
		clockHandler.irqEnabled = false;
		clockHandler.irqMessage = 0;
		clockHandler.tickCounter = 0;
		clockHandler.stop();
	}

	protected final class ClockHandler {

		private volatile int tickCounter = 0;
		private volatile boolean isRunning = false;
		private volatile int sleepDelay = INITIAL_TICKS_PER_SECOND; // 60 HZ

		private volatile boolean irqEnabled = false;
		private volatile int irqMessage = 0;
		
		private int timeSinceTick = 0;
	
		public void update(int deltaTime) {
			if(!isRunning)
				return;
			
			timeSinceTick += deltaTime;
			
			for(; timeSinceTick > sleepDelay; timeSinceTick -= sleepDelay)
			{
				tickCounter++;
				if (irqEnabled && emulator != null) {
					emulator.triggerInterrupt(new HardwareInterrupt(DefaultClock.this, irqMessage));
				}
			}
		}

		public void setTicksPerSecond(int ticksPerSecond) {
			tickCounter = 0;
			out.debug("Clock set to tick " + ticksPerSecond + " times per second");
			sleepDelay = (int) Math.round(1000.0 / ticksPerSecond);
		}
		
		public void stop() {
			timeSinceTick = 0;
			isRunning = false;
		}
		
		public void start() {
			isRunning = true;
		}
	}

	public void update(int deltaTime) {
		clockHandler.update(deltaTime);
	}

	@Override
	public void afterAddDevice(IEmulator emulator) {
		if (this.emulator != null) {
			throw new IllegalStateException("Clock " + this + " already associated with emulator " + emulator + " ?");
		}
		this.emulator = emulator;
		this.emulator.addEmulationListener(myEmulationListener);
		this.out = emulator.getOutput();
	}

	@Override
	public void beforeRemoveDevice(IEmulator emulator) {
		this.emulator.removeEmulationListener(myEmulationListener);
		this.emulator = null;
	}

	@Override
	public DeviceDescriptor getDeviceDescriptor() {
		return DESC;
	}

	@Override
	public int handleInterrupt(IEmulator emulator, ICPU cpu, IMemory memory) {
		/*
         * Name: Generic Clock (compatible)
         * ID: 0x12d0b402
         * Version: 1
         * 
         * Interrupts do different things depending on contents of the A register:
         * 
         *  A | BEHAVIOR
         * ---+----------------------------------------------------------------------------
         *  0 | The B register is read, and the clock will tick 60/B times per second.
         *    | If B is 0, the clock is turned off.
         *  1 | Store number of ticks elapsed since last call to 0 in C register
         *  2 | If register B is non-zero, turn on interrupts with message B. If B is zero,
         *    | disable interrupts
         * ---+----------------------------------------------------------------------------
         * 
         * When interrupts are enabled, the clock will trigger an interrupt whenever it
         * ticks.     
		 */
		final int a = cpu.getRegisterValue(Register.A);
		switch (a) {
			case 0: // The B register is read, and the clock will tick 60/B times per second. If B is 0, the clock is turned off.
				int b = cpu.getRegisterValue(Register.B) & 0xffff;
				if (b == 0) {
					clockHandler.stop();
					return 0;
				}
				if (b < 0) {
					clockHandler.setTicksPerSecond(60);
				} else if (b > 60) {
					clockHandler.setTicksPerSecond(1);
				} else {
					clockHandler.setTicksPerSecond((int) Math.round(60.0 / b));
				}
				clockHandler.start();
				break;
			case 1:
				// Store number of ticks elapsed since last call to 0 in C register
				cpu.setRegisterValue(Register.C, clockHandler.tickCounter & 0xffff);
				break;
			case 2:
				// If register B is non-zero, turn on interrupts with message B. If B is zero, disable interrupts.
				b = cpu.getRegisterValue(Register.B) & 0xffff;
				if (b == 0) {
					clockHandler.irqEnabled = false;
				} else {
					clockHandler.irqMessage = b;
					clockHandler.irqEnabled = true;
					out.debug("Clock IRQs enabled with message " + Misc.toHexString(b));
				}
				break;
			default:
				out.warn("handleInterrupt(): Clock received unknown interrupt msg " + Misc.toHexString(a));
		}
		return 0;
	}

	@Override
	public boolean supportsMultipleInstances() {
		return false;
	}

	@Override
	public String toString() {
		return "'" + DESC.getDescription() + "'";
	}
}
