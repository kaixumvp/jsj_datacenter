package com.jsj.datacenter.infrastructure.common.helper;

import java.io.File;
import java.util.List;

public abstract class FileParser<T> {
    public abstract List<T> parse(File file, Class<T> clazz) throws Exception;
}
