package io.nextflow.pregex

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.plugin.BasePlugin
import org.pf4j.PluginWrapper

/**
 * Nextflow PRegEx Plugin
 * 
 * Provides human-readable regex pattern builders similar to Python's pregex library.
 * Instead of writing complex regex strings, users can use methods like:
 * - Either("foo", "bar") instead of "(foo|bar)"
 * - Literal("text") for escaped literal text
 * - Optional(pattern) instead of "(pattern)?"
 * 
 * @author Marcel Ribeiro-Dantas <marcel@seqera.io>
 */
@Slf4j
@CompileStatic
class PRegExPlugin extends BasePlugin {

    PRegExPlugin(PluginWrapper wrapper) {
        super(wrapper)
    }

    @Override
    void start() {
        log.info "Starting Nextflow PRegEx Plugin v${wrapper.descriptor.version}"
    }

    @Override
    void stop() {
        log.info "Stopping Nextflow PRegEx Plugin"
    }
}
