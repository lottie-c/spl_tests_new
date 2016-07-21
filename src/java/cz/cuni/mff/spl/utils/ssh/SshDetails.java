/*
 * Copyright (c) 2012, František Haas, Martin Lacina, Jaroslav Kotrč, Jiří Daniel
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package cz.cuni.mff.spl.utils.ssh;

import java.util.Map;

import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;

/**
 * <p>
 * This class serves as an authentication information holder for SSH connection.
 * </p>
 * 
 * @author Frantisek Haas
 * 
 */
public class SshDetails {

    /** URL to login to. */
    private final String               url;
    /** Username to login with. */
    private final String               username;
    /** Path to private key. */
    private final String               keyPath;

    /** Whether to trust all hosts. */
    private final boolean              trustAll;
    /** Fingerprint of remote host key to trust to. */
    private final String               fingerprint;
    /** Path to known hosts file. */
    private final String               knownHostsPath;

    /** User interaction object. */
    private final InteractiveInterface interactive;

    /**
     * Parses passed value map into {@link SshDetails} instance.
     * 
     * @param values
     *            Map of values to be parsed.
     * @param interactive
     *            If interaction might be used or {@code null} if not
     *            interaction is allowed.
     * @return
     * @throws SshException
     *             In case crucial values are missing.
     */
    public static SshDetails create(Map<String, String> values, InteractiveInterface interactive)
            throws SshException {

        if (values.containsKey("url") && values.containsKey("username") && values.containsKey("keyPath")) {

            return new SshDetails(
                    values.get("url"),
                    values.get("username"),
                    values.get("keyPath"),
                    Boolean.valueOf(values.get("trustAll")),
                    values.get("fingerprint"),
                    values.get("knownHostsPath"),
                    interactive);

        } else if (values.containsKey("url") && values.containsKey("username")) {
            return new SshDetails(
                    values.get("url"),
                    values.get("username"),
                    Boolean.valueOf(values.get("trustAll")),
                    values.get("fingerprint"),
                    values.get("knownHostsPath"),
                    interactive);

        } else if (!values.containsKey("url")) {
            throw new SshException("Machine information not set correctly. URL is missing.");

        } else if (!values.containsKey("username")) {
            throw new SshException("Machine information not set correctly. Username is missing.");

        } else {
            throw new SshException("Machine information not set correctly.");
        }
    }

    /**
     * <p>
     * Initializes the object for password or interactive authentication.
     * 
     * @param url
     *            URL to login to.
     * @param username
     *            Username to login with.
     * @param trustAll
     *            Whether to trust all hosts.
     * @param fingerprint
     *            Fingerprint of remote host key to trust to.
     * @param knownHostsPath
     *            Path to known hosts file.
     * @param interactive
     *            User interaction object.
     */
    public SshDetails(String url, String username, boolean trustAll, String fingerprint, String knownHostsPath, InteractiveInterface interactive) {
        this.url = url;
        this.username = username;
        this.keyPath = null;

        this.trustAll = trustAll;
        this.fingerprint = fingerprint;
        this.knownHostsPath = knownHostsPath;

        this.interactive = interactive;
    }

    /**
     * <p>
     * Initializes the object for key authentication.
     * 
     * @param url
     *            URL to login to.
     * @param username
     *            Username to login with.
     * @param keyPath
     *            Path to private key.
     * @param trustAll
     *            Whether to trust all hosts.
     * @param fingerprint
     *            Fingerprint of remote host key to trust to.
     * @param knownHostsPath
     *            Path to known hosts file.
     * @param interactive
     *            User interaction object.
     */
    public SshDetails(String url, String username, String keyPath, boolean trustAll, String fingerprint, String knownHostsPath, InteractiveInterface interactive) {
        this.url = url;
        this.username = username;
        this.keyPath = keyPath;

        this.trustAll = trustAll;
        this.fingerprint = fingerprint;
        this.knownHostsPath = knownHostsPath;

        this.interactive = interactive;
    }

    /**
     * @return
     *         URL of the host.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return
     *         User name to login with.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return
     *         Path to the authentication key.
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * @return
     *         Whether to trust all hosts or not.
     */
    public boolean getTrustAll() {
        return trustAll;
    }

    /**
     * @return
     *         Fingerprint or sub string of fingerprint of host to trust.
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * @return
     *         Known hosts file.
     */
    public String getKnownHostsPath() {
        return knownHostsPath;
    }

    /**
     * @return
     *         Whether interactive prompts will be processed with the user.
     */
    public boolean isInteractive() {
        return interactive != null && interactive.isInteractive();
    }

    /**
     * Prompts user for interactive data if interaction is enabled.
     * 
     * @param prompt
     *            The message to show to user.
     * @return
     *         Value filled by user or empty string if interaction is not
     *         enabled.
     */
    public String getString(String prompt) {
        if (interactive != null) {
            return interactive.getString(prompt);
        } else {
            return "";
        }
    }

    /**
     * Prompts user for sensitive interactive data if interaction is enabled.
     * 
     * @param prompt
     *            The message to show to user.
     * @return
     *         Value filled by user or empty string if interaction is not
     *         enabled.
     */
    public String getMaskedString(String prompt) {
        if (interactive != null) {
            return interactive.getMaskedString(prompt);
        } else {
            return "";
        }
    }

    /**
     * Prompts user for sensitive interactive yes / no if interaction is
     * enabled.
     * 
     * @param prompt
     *            The message to show to user.
     * @return
     *         Value filled by user or false if interaction is not enabled.
     */
    public Boolean getBoolean(String prompt) {
        if (interactive != null) {
            return interactive.getBoolean(prompt);
        } else {
            return false;
        }
    }
}
