package com.jay.template.infra.outbound.http.client.rest.adapter.ping.mapping;

import com.jay.template.infra.outbound.http.client.rest.adapter.ping.contract.DownstreamPingResponse;
import com.jay.template.core.domain.dependency.ping.PingResult;

public class DownstreamPingResponseMapper {

    public PingResult map(DownstreamPingResponse response) {

        if (response == null) {
            return new PingResult(false, "");
        }

        String msg = response.msg();

        if (msg == null) {
            msg = "";
        }

        boolean ok = "pong".equals(response.msg());

        return new PingResult(ok, msg);
    }
}
