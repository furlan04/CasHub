package it.unimib.CasHub.source.portfolio;

import java.util.List;
import it.unimib.CasHub.model.PortfolioStock;

public interface IPortfolioCallback {
    void onPortfolioSuccess(List<PortfolioStock> portfolio);
    void onPortfolioFailure(Exception e);
}
