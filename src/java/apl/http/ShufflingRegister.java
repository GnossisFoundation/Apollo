/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2018 Apollo Foundation
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

import apl.Account;
import apl.Attachment;
import apl.AplException;
import apl.Shuffling;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class ShufflingRegister extends CreateTransaction {

    static final ShufflingRegister instance = new ShufflingRegister();

    private ShufflingRegister() {
        super(new APITag[] {APITag.SHUFFLING, APITag.CREATE_TRANSACTION}, "shufflingFullHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws AplException {
        byte[] shufflingFullHash = ParameterParser.getBytes(req, "shufflingFullHash", true);

        Attachment attachment = new Attachment.ShufflingRegistration(shufflingFullHash);

        Account account = ParameterParser.getSenderAccount(req);
        if (account.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            return JSONResponses.error("Accounts under phasing only control cannot join a shuffling");
        }
        try {
            return createTransaction(req, account, attachment);
        } catch (AplException.InsufficientBalanceException e) {
            Shuffling shuffling = Shuffling.getShuffling(shufflingFullHash);
            if (shuffling == null) {
                return JSONResponses.NOT_ENOUGH_FUNDS;
            }
            return JSONResponses.notEnoughHolding(shuffling.getHoldingType());
        }
    }

}
