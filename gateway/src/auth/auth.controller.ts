import { Controller, Post, Req, Res } from '@nestjs/common';
import { Request, Response } from 'express';
import { firstValueFrom } from 'rxjs';
import { Public } from './public.decorator';
import { ProxyService } from '../proxy/proxy.service';
import { ValidatedUser } from './jwt.strategy';

@Controller('auth')
export class AuthController {
  constructor(private proxyService: ProxyService) {}

  private async forwardRequest(req: Request, res: Response): Promise<void> {
    const pseudoUser: ValidatedUser = {
      userId: 'anonymous',
      role: 'GUEST',
    };

    try {
      const response = await firstValueFrom(this.proxyService.forward(req, pseudoUser));
      if (response?.status !== undefined && response?.data !== undefined) {
        res.status(response.status).json(response.data);
      } else {
        res.status(500).json({ error: 'AUTH_ERROR', message: 'Invalid response from backend' });
      }
    } catch (error: unknown) {
      const axiosError = error as any;
      const status = axiosError?.response?.status || 500;
      const data = axiosError?.response?.data || {
        error: 'AUTH_ERROR',
        message: axiosError?.message || 'Unknown error'
      };
      res.status(status).json(data);
    }
  }

  @Public()
  @Post('login')
  async login(@Req() req: Request, @Res() res: Response): Promise<void> {
    await this.forwardRequest(req, res);
  }

  @Public()
  @Post('mfa/verify')
  async verifyMfa(@Req() req: Request, @Res() res: Response): Promise<void> {
    await this.forwardRequest(req, res);
  }

  @Public()
  @Post('refresh')
  async refresh(@Req() req: Request, @Res() res: Response): Promise<void> {
    await this.forwardRequest(req, res);
  }

  @Public()
  @Post('logout')
  async logout(@Req() req: Request, @Res() res: Response): Promise<void> {
    await this.forwardRequest(req, res);
  }
}
