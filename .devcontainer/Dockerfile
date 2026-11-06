FROM archlinux:base-devel

# package installs.
RUN pacman -Syu --noconfirm \
    bash \
    git \
    openssh \
    ca-certificates \
    curl \
    wget \
    sudo \
    which \
    procps-ng \
    less \
    man-db \
    man-pages \
    nano \
    ripgrep \
    fd \
    jq \
    unzip \
    tar \
    gzip \
    tealdeer \
    libx11 \
    libxext \
    libxi \
    libxrender \
    libxtst \
    fontconfig \
    ttf-dejavu \
    jdk25-openjdk \
    maven \
 && pacman -Scc --noconfirm

# create the user and workspace directory.
RUN useradd -m -s /bin/bash dev \
 && echo "dev ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/dev \
 && chmod 0440 /etc/sudoers.d/dev \
 && install -d -o dev -g dev -m 0755 /home/dev/code

ENV HOME=/home/dev
ENV SHELL=/bin/bash
ENV PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# install some basic bash settings.
COPY --chown=dev:dev .bashrc /home/dev/.bashrc

# set the local working directory.
USER dev
WORKDIR /home/dev/code
