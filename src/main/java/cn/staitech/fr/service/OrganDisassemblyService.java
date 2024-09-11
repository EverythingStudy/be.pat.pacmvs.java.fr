package cn.staitech.fr.service;

import java.io.IOException;
import java.util.List;

public interface OrganDisassemblyService {

    void export(List<Long> imageIds) throws IOException;

}
