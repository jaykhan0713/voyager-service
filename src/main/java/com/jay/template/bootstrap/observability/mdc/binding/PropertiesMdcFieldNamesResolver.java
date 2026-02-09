package com.jay.template.bootstrap.observability.mdc.binding;

import org.springframework.stereotype.Component;

import com.jay.template.bootstrap.observability.properties.ObservabilityProperties;
import com.jay.template.core.observability.mdc.MdcFieldNames;
import com.jay.template.core.port.observability.mdc.MdcFieldNamesProvider;

@Component
public class PropertiesMdcFieldNamesResolver implements MdcFieldNamesProvider {

    final MdcFieldNames mdcFieldNames;

    public PropertiesMdcFieldNamesResolver(ObservabilityProperties obsProps) {
        var propsMdc = obsProps.mdc();
        var propsKindValues = propsMdc.kindValues();

        var kindValues = new MdcFieldNames.KindValues(propsKindValues.http());

        this.mdcFieldNames = new MdcFieldNames(
                propsMdc.userId(),
                propsMdc.requestId(),
                propsMdc.kind(),
                propsMdc.name(),
                propsMdc.method(),
                propsMdc.status(),
                propsMdc.durationMs(),
                kindValues
        );
    }

    @Override
    public MdcFieldNames mdcFieldNames() {
        return mdcFieldNames;
    }
}
