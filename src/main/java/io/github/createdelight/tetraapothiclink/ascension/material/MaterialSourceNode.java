package io.github.createdelight.tetraapothiclink.ascension.material;

/** A reload-scoped immutable recipe for only the material-derived attribute/effect channels. */
public interface MaterialSourceNode {
    MaterialChannels evaluate(float primaryFactor, float secondaryFactor, float tertiaryFactor);
}
