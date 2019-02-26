/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul;

import javax.validation.constraints.NotNull;

import com.ecwid.consul.transport.TLSConfig.KeyStoreInstanceType;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;
import org.springframework.validation.annotation.Validated;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.consul")
@Validated
public class ConsulProperties {

	/** Consul agent hostname. Defaults to 'localhost'. */
	@NotNull
	private String host = "localhost";

	/**
	 * Consul agent scheme (HTTP/HTTPS). If there is no scheme in address - client will
	 * use HTTP.
	 */
	private String scheme;

	/** Consul agent port. Defaults to '8500'. */
	@NotNull
	private int port = 8500;

	/** Is spring cloud consul enabled. */
	private boolean enabled = true;

	/** configuration for TLS. */
	private TLSConfig tls;

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getScheme() {
		return this.scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public TLSConfig getTls() {
		return this.tls;
	}

	public void setTls(TLSConfig tls) {
		this.tls = tls;
	}

	@Override
	public String toString() {
		return "ConsulProperties{" + "host='" + this.host + '\'' + ", port=" + this.port
				+ ", scheme=" + this.scheme + ", tls=" + this.tls + ", enabled="
				+ this.enabled + '}';
	}

	/**
	 * TLS configuration.
	 */
	public static class TLSConfig {

		/** Type of key framework to use. */
		private KeyStoreInstanceType keyStoreInstanceType;

		/** Path to an external keystore. */
		private String keyStorePath;

		/** Password to an external keystore. */
		private String keyStorePassword;

		/** File path to the certificate. */
		private String certificatePath;

		/** Password to open the certificate. */
		private String certificatePassword;

		public TLSConfig() {
		}

		public TLSConfig(KeyStoreInstanceType keyStoreInstanceType, String keyStorePath,
				String keyStorePassword, String certificatePath,
				String certificatePassword) {
			this.keyStoreInstanceType = keyStoreInstanceType;
			this.keyStorePath = keyStorePath;
			this.keyStorePassword = keyStorePassword;
			this.certificatePath = certificatePath;
			this.certificatePassword = certificatePassword;
		}

		public KeyStoreInstanceType getKeyStoreInstanceType() {
			return this.keyStoreInstanceType;
		}

		public void setKeyStoreInstanceType(KeyStoreInstanceType keyStoreInstanceType) {
			this.keyStoreInstanceType = keyStoreInstanceType;
		}

		public String getKeyStorePath() {
			return this.keyStorePath;
		}

		public void setKeyStorePath(String keyStorePath) {
			this.keyStorePath = keyStorePath;
		}

		public String getKeyStorePassword() {
			return this.keyStorePassword;
		}

		public void setKeyStorePassword(String keyStorePassword) {
			this.keyStorePassword = keyStorePassword;
		}

		public String getCertificatePath() {
			return this.certificatePath;
		}

		public void setCertificatePath(String certificatePath) {
			this.certificatePath = certificatePath;
		}

		public String getCertificatePassword() {
			return this.certificatePassword;
		}

		public void setCertificatePassword(String certificatePassword) {
			this.certificatePassword = certificatePassword;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this)
					.append("keyStoreInstanceType", this.keyStoreInstanceType)
					.append("keyStorePath", this.keyStorePath)
					.append("keyStorePassword", this.keyStorePassword)
					.append("certificatePath", this.certificatePath)
					.append("certificatePassword", this.certificatePassword).toString();
		}

	}

}
