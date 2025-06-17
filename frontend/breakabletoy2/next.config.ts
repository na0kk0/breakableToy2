import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint:{
    ignoreDuringBuilds:true,
  },
  async redirects () {
    return [{
      source: "/",
      destination: "/search",
      permanent: true,
    }];
  }
};

export default nextConfig;
