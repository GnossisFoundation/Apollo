/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017-2018 Apollo Foundation
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Apollo Foundation,
 * no part of the Apl software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package apl.http;

import apl.AplException;
import apl.peer.Peer;
import apl.peer.Peers;
import apl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static apl.http.JSONResponses.API_PROXY_NO_OPEN_API_PEERS;
import static apl.http.JSONResponses.PEER_NOT_CONNECTED;
import static apl.http.JSONResponses.PEER_NOT_OPEN_API;
import static apl.http.JSONResponses.UNKNOWN_PEER;

public class SetAPIProxyPeer extends APIServlet.APIRequestHandler {

    static final SetAPIProxyPeer instance = new SetAPIProxyPeer();

    private SetAPIProxyPeer() {
        super(new APITag[] {APITag.NETWORK}, "peer");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws AplException {
        String peerAddress = Convert.emptyToNull(request.getParameter("peer"));
        if (peerAddress == null) {
            Peer peer = APIProxy.getInstance().setForcedPeer(null);
            if (peer == null) {
                return API_PROXY_NO_OPEN_API_PEERS;
            }
            return JSONData.peer(peer);
        }
        Peer peer = Peers.findOrCreatePeer(peerAddress, false);
        if (peer == null) {
            return UNKNOWN_PEER;
        }
        if (peer.getState() != Peer.State.CONNECTED ) {
            return PEER_NOT_CONNECTED;
        }
        if (!peer.isOpenAPI()) {
            return PEER_NOT_OPEN_API;
        }
        APIProxy.getInstance().setForcedPeer(peer);
        return JSONData.peer(peer);
    }

    @Override
    protected boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }


}
